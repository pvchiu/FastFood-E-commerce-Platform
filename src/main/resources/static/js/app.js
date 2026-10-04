// FastFood App JavaScript

document.addEventListener("DOMContentLoaded", function () {
  initializeApp();
});

function initializeApp() {
  initializeCartFunctionality();
  initializeFormValidation();
  initializeTooltips();
  initializeNavigation();
  initializeRoleBasedUI();
  initializePasswordToggle();
}

// Cart Functionality
function initializeCartFunctionality() {
  const addToCartButtons = document.querySelectorAll(".add-to-cart");

  addToCartButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const productId = this.getAttribute("data-product-id");
      addToCart(productId);
    });
  });
}

function addToCart(productId) {
  // Show loading state
  const button = document.querySelector(`[data-product-id="${productId}"]`);
  const originalText = button.innerHTML;
  button.innerHTML = '<span class="loading"></span>';
  button.disabled = true;

  // Simulate API call
  setTimeout(() => {
    // Reset button
    button.innerHTML = originalText;
    button.disabled = false;

    // Show success message
    showNotification("Added to cart", "success");

    // Update cart count (if exists)
    updateCartCount();
  }, 1000);
}

function updateCartCount() {
  const cartBadge = document.querySelector(".cart-badge");
  if (cartBadge) {
    const currentCount = parseInt(cartBadge.textContent) || 0;
    cartBadge.textContent = currentCount + 1;

    // Animate the badge
    cartBadge.style.transform = "scale(1.3)";
    setTimeout(() => {
      cartBadge.style.transform = "scale(1)";
    }, 200);
  }
}

// Form Validation
function initializeFormValidation() {
  const forms = document.querySelectorAll(".needs-validation");

  forms.forEach((form) => {
    form.addEventListener("submit", function (event) {
      if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      }
      form.classList.add("was-validated");
    });
  });
}

// Tooltips
function initializeTooltips() {
  const tooltipTriggerList = [].slice.call(
    document.querySelectorAll('[data-bs-toggle="tooltip"]')
  );
  tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl);
  });
}

// Navigation
function initializeNavigation() {
  // Highlight current page in navigation
  const currentPath = window.location.pathname;
  const navLinks = document.querySelectorAll(".navbar-nav .nav-link");

  navLinks.forEach((link) => {
    if (link.getAttribute("href") === currentPath) {
      link.classList.add("active");
    }
  });
}

// Notification System
function showNotification(message, type = "info") {
  const notification = document.createElement("div");
  notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
  notification.style.cssText =
    "top: 20px; right: 20px; z-index: 9999; min-width: 300px;";

  notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

  document.body.appendChild(notification);

  // Auto remove after 3 seconds
  setTimeout(() => {
    if (notification.parentNode) {
      notification.remove();
    }
  }, 3000);
}

// Search Functionality
function initializeSearch() {
  const searchInput = document.querySelector("#search-input");
  const searchButton = document.querySelector("#search-button");

  if (searchInput && searchButton) {
    searchButton.addEventListener("click", performSearch);
    searchInput.addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        performSearch();
      }
    });
  }
}

function performSearch() {
  const searchTerm = document.querySelector("#search-input").value.trim();
  if (searchTerm) {
    // Redirect to search results page
    window.location.href = `/products?search=${encodeURIComponent(searchTerm)}`;
  }
}

// Cart Management
function initializeCartPage() {
  initializeQuantityControls();
  initializeRemoveItems();
  initializeCouponCode();
  initializeCheckout();
  updateCartSummary();
}

// // Quantity Controls
// function initializeQuantityControls() {
//   document.querySelectorAll(".qty-increase").forEach((btn) => {
//     btn.addEventListener("click", function () {
//       const container = this.closest(".input-group");
//       const input = container.querySelector(".qty-input");
//       let currentValue = parseInt(input.value);
//       if (currentValue < 99) {
//         input.value = currentValue + 1;
//         updateItemPrice(this.closest(".cart-item"));
//         updateCartSummary();
//       }
//     });
//   });

//   document.querySelectorAll(".qty-decrease").forEach((btn) => {
//     btn.addEventListener("click", function () {
//       const input = this.nextElementSibling;
//       let currentValue = parseInt(input.value);
//       if (currentValue > 1) {
//         input.value = currentValue - 1;
//         updateItemPrice(this.closest(".cart-item"));
//         updateCartSummary();
//       }
//     });
//   });
// }

// // Remove Items
// // function initializeRemoveItems() {
// //    document.querySelectorAll('.remove-item').forEach(btn => {
// //       btn.addEventListener('click', function () {
// //          const cartItem = this.closest('.cart-item');
// //          const productName = cartItem.querySelector('h6').textContent;

// //          if (confirm(`Bạn có chắc muốn xóa "${productName}" khỏi giỏ hàng?`)) {
// //             removeCartItem(cartItem);
// //          }
// //       });
// //    });
// // }

function removeCartItem(cartItem) {
  cartItem.style.animation = "slideOut 0.3s ease-in-out";

  setTimeout(() => {
    cartItem.remove();
    updateCartCount();
    updateCartSummary();
    checkEmptyCart();
    showNotification("Product removed from cart", "success");
  }, 300);
}

// Update Item Price
function updateItemPrice(cartItem) {
  const qtyInput = cartItem.querySelector(".qty-input");
  const priceElement = cartItem.querySelector(".item-price");
  const unitPrice = parseInt(priceElement.dataset.unitPrice);
  const quantity = parseInt(qtyInput.value);
  const totalPrice = unitPrice * quantity;

  priceElement.textContent = formatPrice(totalPrice);
  priceElement.classList.add("price-update");

  setTimeout(() => {
    priceElement.classList.remove("price-update");
  }, 500);
}

// Update Cart Summary
function updateCartSummary() {
  let subtotal = 0;

  document.querySelectorAll(".cart-item").forEach((item) => {
    const priceElement = item.querySelector(".item-price");
    const unitPrice = parseInt(priceElement.dataset.unitPrice);
    const quantity = parseInt(item.querySelector(".qty-input").value);
    subtotal += unitPrice * quantity;
  });

  const shippingFee = subtotal > 200000 ? 0 : 20000; // Free shipping over 200k
  const discount = calculateDiscount(subtotal);
  const total = subtotal + shippingFee - discount;

  document.getElementById("subtotal").textContent = formatPrice(subtotal);
  document.getElementById("shipping-fee").textContent =
    formatPrice(shippingFee);
  document.getElementById("discount").textContent = "-" + formatPrice(discount);
  document.getElementById("total").textContent = formatPrice(total);

  // Update shipping message
  updateShippingMessage(subtotal);
}

function updateShippingMessage(subtotal) {
  const shippingFee = document.getElementById("shipping-fee");
  if (subtotal > 200000) {
    shippingFee.innerHTML = '<span class="text-success">Miễn phí</span>';
  } else {
    const remaining = 200000 - subtotal;
    shippingFee.parentElement.innerHTML = `
            <span>Phí giao hàng:</span>
            <div>
                <span>${formatPrice(20000)}</span><br>
                <small class="text-muted">Buy ${formatPrice(
      remaining
    )} more for free shipping</small>
            </div>
        `;
  }
}

// Coupon Code
function initializeCouponCode() {
  const applyCouponBtn = document.getElementById("apply-coupon");
  const couponInput = document.getElementById("coupon-code");

  applyCouponBtn.addEventListener("click", function () {
    const couponCode = couponInput.value.trim().toUpperCase();
    applyCoupon(couponCode);
  });

  couponInput.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
      const couponCode = this.value.trim().toUpperCase();
      applyCoupon(couponCode);
    }
  });
}

function applyCoupon(code) {
  const validCoupons = {
    WELCOME10: 10, // 10% discount
    FASTFOOD20: 20000, // 20k discount
    NEWUSER: 15000, // 15k discount
  };

  if (validCoupons[code]) {
    const discount = validCoupons[code];
    document.getElementById("coupon-code").classList.add("coupon-applied");

    if (typeof discount === "number" && discount < 100) {
      // Percentage discount
      showNotification(
        `${discount}% discount code applied successfully!`,
        "success"
      );
    } else {
      // Fixed amount discount
      showNotification(
        `${formatPrice(discount)} discount code applied successfully!`,
        "success"
      );
    }

    updateCartSummary();

    setTimeout(() => {
      document.getElementById("coupon-code").classList.remove("coupon-applied");
    }, 500);
  } else {
    showNotification("Invalid discount code!", "error");
    document.getElementById("coupon-code").focus();
  }
}

function calculateDiscount(subtotal) {
  const couponCode = document
    .getElementById("coupon-code")
    .value.trim()
    .toUpperCase();
  const validCoupons = {
    WELCOME10: subtotal * 0.1,
    FASTFOOD20: 20000,
    NEWUSER: 15000,
  };

  return validCoupons[couponCode] || 0;
}

// Checkout
function initializeCheckout() {
  document
    .getElementById("checkout-btn")
    .addEventListener("click", function () {
      const cartItems = document.querySelectorAll(".cart-item");
      if (cartItems.length === 0) {
        showNotification("Cart is empty!", "error");
        return;
      }

      // Show loading state
      this.innerHTML = '<span class="loading"></span> Processing...';
      this.disabled = true;

      // Simulate processing
      setTimeout(() => {
        // Reset button
        this.innerHTML = '<i class="fas fa-credit-card me-2"></i>Checkout';
        this.disabled = false;

        // Redirect to checkout page (future)
        showNotification("Redirecting to checkout page...", "info");
        // window.location.href = '/cart/checkout';
      }, 2000);
    });
}

// Update Cart Count
function updateCartCount() {
  const cartItems = document.querySelectorAll(".cart-item");
  const countElement = document.getElementById("cart-count");

  if (countElement) {
    countElement.textContent = cartItems.length;

    // Animate count change
    countElement.style.transform = "scale(1.3)";
    setTimeout(() => {
      countElement.style.transform = "scale(1)";
    }, 200);
  }
}

// Check Empty Cart
function checkEmptyCart() {
  const cartItems = document.querySelectorAll(".cart-item");
  const cartWithItems = document.getElementById("cart-with-items");
  const emptyCart = document.getElementById("empty-cart");

  if (cartItems.length === 0) {
    cartWithItems.style.display = "none";
    emptyCart.style.display = "block";
    emptyCart.style.animation = "fadeIn 0.5s ease-in-out";
  }
}

// Format Price Helper
function formatPrice(price) {
  return "$" + parseFloat(price).toFixed(2);
}

// CSS Animations
const style = document.createElement("style");
style.textContent = `
    @keyframes slideOut {
        from { opacity: 1; transform: translateX(0); }
        to { opacity: 0; transform: translateX(100%); }
    }
    
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }
`;
document.head.appendChild(style);

// Role-based UI Control
function initializeRoleBasedUI() {
  // Set body data attribute for CSS targeting
  const userRole = document.querySelector('meta[name="user-role"]')?.getAttribute('content');
  if (userRole) {
    document.body.setAttribute('data-user-role', userRole);
  }

  // Lấy thông tin user từ meta tag hoặc gọi API
  checkUserRoleAndHideAdminFeatures();
}

function checkUserRoleAndHideAdminFeatures() {
  // Lấy role từ meta tag
  const userRole = document.querySelector('meta[name="user-role"]')?.getAttribute('content');

  if (userRole) {
    hideAdminFeaturesForNonManager(userRole);
  } else {
    // Fallback: gọi API để lấy thông tin user hiện tại
    getCurrentUserRole().then(role => {
      hideAdminFeaturesForNonManager(role);
    });
  }
}

async function getCurrentUserRole() {
  try {
    const response = await fetch('/api/user/current', {
      method: 'GET',
      credentials: 'same-origin'
    });

    if (response.ok) {
      const userData = await response.json();
      return userData.role;
    }
  } catch (error) {
    console.log('Could not fetch user role, assuming USER role');
  }
  return 'ROLE_USER'; // Default to USER if can't determine
}

function hideAdminFeaturesForNonManager(userRole) {
  // Nếu không phải MANAGER hoặc ADMIN, ẩn tất cả admin features
  if (userRole !== 'ROLE_MANAGER' && userRole !== 'ROLE_ADMIN') {

    // Ẩn Admin Management dropdown
    const adminDropdown = document.querySelector('#adminDropdown');
    if (adminDropdown) {
      adminDropdown.closest('li').style.display = 'none';
    }

    // Ẩn các button/link admin trong các trang khác
    const adminButtons = document.querySelectorAll('.admin-only, .manager-only');
    adminButtons.forEach(btn => {
      btn.style.display = 'none';
    });

    // Redirect nếu đang ở trang admin mà không có quyền
    const currentPath = window.location.pathname;
    if (currentPath.includes('/manager/') || currentPath.includes('/admin/')) {
      window.location.href = '/';
      showNotification('Access denied. Manager privileges required.', 'error');
    }
  }
}

// Global Password Toggle Function
function togglePassword(inputId) {
  const input = document.getElementById(inputId);
  const toggle = document.getElementById(inputId + 'Toggle');

  if (!input || !toggle) return;

  if (input.type === 'password') {
    input.type = 'text';
    toggle.className = 'fas fa-eye-slash';
    toggle.closest('button').setAttribute('title', 'Hide password');
  } else {
    input.type = 'password';
    toggle.className = 'fas fa-eye';
    toggle.closest('button').setAttribute('title', 'Show password');
  }
}

// Initialize Password Toggle Functionality
function initializePasswordToggle() {
  // Auto-detect password toggle buttons and set up functionality
  const toggleButtons = document.querySelectorAll('[id^="toggle"][id$="Password"]');
  
  toggleButtons.forEach(button => {
    const inputId = button.id.replace('toggle', '').replace(/^./, str => str.toLowerCase());
    const input = document.getElementById(inputId);
    
    if (input && input.type === 'password') {
      button.addEventListener('click', () => togglePassword(inputId));
      button.setAttribute('title', 'Show password');
    }
  });
}

// Initialize cart page if we're on cart page
if (window.location.pathname.includes("/cart")) {
  document.addEventListener("DOMContentLoaded", initializeCartPage);
}
