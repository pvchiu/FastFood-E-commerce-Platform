package com.example.myproject.controller;

import com.example.myproject.entity.*;
import com.example.myproject.entity.OrderItem.PaymentMethod;
import com.example.myproject.repository.*;
import com.example.myproject.service.CartItemService;
import com.example.myproject.service.CurrencyService;
import com.example.myproject.service.VNPayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public String showCheckoutPage(Model model, Principal principal,
            @RequestParam(value = "error", required = false) String error) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        if (error != null) {
            switch (error) {
                case "payment_failed":
                    model.addAttribute("errorMessage", "Payment failed! Please try again.");
                    break;
                case "session_expired":
                    model.addAttribute("errorMessage", "Session expired! Please try again.");
                    break;
                case "order_not_found":
                    model.addAttribute("errorMessage", "Order not found! Please try again.");
                    break;
                case "invalid_signature":
                    model.addAttribute("errorMessage", "Payment verification failed! Please try again.");
                    break;
                case "processing_error":
                    model.addAttribute("errorMessage", "Processing error! Please try again.");
                    break;
            }
        }

        return "checkout";
    }

    @PostMapping
    public String processCheckout(
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("deliveryAddress") String deliveryAddress,
            @RequestParam(value = "notes", required = false) String notes,
            Principal principal,
            HttpSession session,
            HttpServletRequest request) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<CartItem> cartItems = cartItemService.getCartItemsByUser(user);

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setDeliveryAddress(deliveryAddress);
        order.setNotes(notes);
        order.setTotalAmount(total);
        order.setPaymentMethod(OrderItem.PaymentMethod.valueOf(paymentMethod));
        order.setStatus(OrderItem.OrderStatus.PENDING);
        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getProduct().getPrice());
            orderItem.setSubtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemRepository.save(orderItem);
        }

        session.setAttribute("pendingOrderId", order.getId());

        if ("ONLINE_BANKING".equals(paymentMethod)) {
            BigDecimal vndAmount = currencyService.convertUsdToVnd(total);
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String vnpayUrl = vnPayService.createOrder(vndAmount.intValue(),
                    "Payment for order " + order.getOrderNumber(), baseUrl);
            return "redirect:" + vnpayUrl;
        } else {
            cartItemService.clearCart(user);
            session.removeAttribute("pendingOrderId");
            return "redirect:/checkout/order/success?orderId=" + order.getId();
        }
    }

    @GetMapping("/vnpay-return")
    public String vnPayReturn(HttpServletRequest request, HttpSession session, Principal principal) {
        try {
            int paymentStatus = vnPayService.orderReturn(request);

            Long orderId = (Long) session.getAttribute("pendingOrderId");
            if (orderId == null) {
                System.out.println("No pending order ID in session");
                return "redirect:/checkout?error=session_expired";
            }

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                System.out.println("Order not found: " + orderId);
                session.removeAttribute("pendingOrderId");
                return "redirect:/checkout?error=order_not_found";
            }

            Order order = orderOpt.get();
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (paymentStatus == 1) {
                System.out.println("Payment successful for order: " + order.getOrderNumber());
                order.setStatus(OrderItem.OrderStatus.PENDING);
                orderRepository.save(order);

                cartItemService.clearCart(user);
                session.removeAttribute("pendingOrderId");

                return "redirect:/checkout/order/success?orderId=" + order.getId();

            } else if (paymentStatus == 0) {
                System.out.println("Payment failed for order: " + order.getOrderNumber());

                orderRepository.delete(order);
                session.removeAttribute("pendingOrderId");

                return "redirect:/checkout?error=payment_failed";

            } else {
                System.out.println("Invalid signature for order: " + order.getOrderNumber());

                orderRepository.delete(order);
                session.removeAttribute("pendingOrderId");

                return "redirect:/checkout?error=invalid_signature";
            }

        } catch (Exception e) {
            System.err.println("Error processing VNPay return: " + e.getMessage());
            e.printStackTrace();

            session.removeAttribute("pendingOrderId");
            return "redirect:/checkout?error=processing_error";
        }
    }

    @GetMapping("/order/success")
    public String showOrderSuccess(@RequestParam("orderId") Long orderId, Model model, Principal principal) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/";
        }

        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "success";
    }

}