/**
 * Product Detail Page JavaScript
 * Handles product interactions, price calculations, and cart operations
 */

document.addEventListener('DOMContentLoaded', function () {

   console.log('🚀 Product Detail JS Loaded Successfully!');

   // Page elements
   const quantityInput = document.getElementById('quantity');
   const totalPriceElement = document.getElementById('totalPrice');
   const addToCartBtn = document.getElementById('addToCartBtn');
   const mainProductImage = document.getElementById('mainProductImage');
   const modalImage = document.getElementById('modalImage');

   // Product configuration
   let productConfig = {
      basePrice: 89.00, // Will be updated from backend
      currentPrice: 89.00,
      selectedSize: 'small',
      selectedAddons: [],
      quantity: 1
   };

   // Size price modifiers
   const sizePrices = {
      'small': 0,
      'medium': 1.50,
      'large': 2.50
   };

   // Addon prices
   const addonPrices = {
      'cheese': 1.00,
      'bacon': 1.50,
      'extra-meat': 2.00
   };

   // Initialize page
   initializeProductDetail();

   /**
    * Initialize product detail functionality
    */
   function initializeProductDetail() {
      // Set initial price from template
      const priceElement = document.querySelector('.current-price');
      if (priceElement) {
         const priceText = priceElement.textContent.replace(/[^\d.]/g, '');
         productConfig.basePrice = parseFloat(priceText) || 89.00;
         productConfig.currentPrice = productConfig.basePrice;
      }

      // Initialize event listeners
      initializeEventListeners();

      // Update initial total price
      updateTotalPrice();

      // Initialize thumbnail interactions
      initializeThumbnails();

      // Initialize size options
      initializeSizeOptions();

      // Initialize addon options
      initializeAddonOptions();
   }

   /**
    * Initialize all event listeners
    */
   function initializeEventListeners() {
      console.log('🔧 Initializing event listeners...');

      // Quantity input change
      if (quantityInput) {
         quantityInput.addEventListener('change', function () {
            productConfig.quantity = parseInt(this.value) || 1;
            updateTotalPrice();
            console.log('Quantity changed to:', productConfig.quantity);
         });
      }

      // Image modal
      if (mainProductImage && modalImage) {
         mainProductImage.addEventListener('click', function () {
            modalImage.src = this.src;
         });
      }

      // Size options
      document.querySelectorAll('input[name="size"]').forEach(radio => {
         radio.addEventListener('change', handleSizeChange);
         console.log('Size radio listener added for:', radio.value);
      });

      // Addon options
      document.querySelectorAll('.addon-options input[type="checkbox"]').forEach(checkbox => {
         checkbox.addEventListener('change', handleAddonChange);
         console.log('Addon listener added for:', checkbox.value);
      });

      console.log('✅ Event listeners initialized');
   }

   /**
    * Initialize thumbnail image interactions
    */
   function initializeThumbnails() {
      document.querySelectorAll('.thumbnail-image').forEach(thumbnail => {
         thumbnail.addEventListener('click', function () {
            changeMainImage(this.src);
         });

         // Add keyboard support
         thumbnail.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
               e.preventDefault();
               changeMainImage(this.src);
            }
         });

         // Make focusable
         thumbnail.setAttribute('tabindex', '0');
         thumbnail.setAttribute('role', 'button');
      });
   }

   /**
    * Initialize size option interactions
    */
   function initializeSizeOptions() {
      document.querySelectorAll('input[name="size"]').forEach(radio => {
         if (radio.checked) {
            productConfig.selectedSize = radio.value;
         }
      });
   }

   /**
    * Initialize addon option interactions
    */
   function initializeAddonOptions() {
      document.querySelectorAll('.addon-options input[type="checkbox"]').forEach(checkbox => {
         if (checkbox.checked) {
            productConfig.selectedAddons.push(checkbox.value);
         }
      });
   }

   /**
    * Change main product image
    */
   window.changeMainImage = function (newSrc) {
      if (mainProductImage) {
         mainProductImage.src = newSrc;

         // Update modal image
         if (modalImage) {
            modalImage.src = newSrc;
         }

         // Update active thumbnail
         document.querySelectorAll('.thumbnail-image').forEach(thumb => {
            thumb.classList.remove('active');
            if (thumb.src === newSrc) {
               thumb.classList.add('active');
            }
         });

         // Add animation
         mainProductImage.style.opacity = '0.7';
         setTimeout(() => {
            mainProductImage.style.opacity = '1';
         }, 150);
      }
   };

   /**
    * Handle size selection change
    */
   function handleSizeChange(e) {
      console.log('🔧 Size changed to:', e.target.value);
      productConfig.selectedSize = e.target.value;
      updateTotalPrice();

      // Visual feedback
      const sizeLabels = document.querySelectorAll('.size-option label');
      sizeLabels.forEach(label => label.classList.remove('active'));
      e.target.nextElementSibling.classList.add('active');
   }

   /**
    * Handle addon selection change
    */
   function handleAddonChange(e) {
      const addonValue = e.target.value;
      console.log('🔧 Addon changed:', addonValue, 'checked:', e.target.checked);

      if (e.target.checked) {
         if (!productConfig.selectedAddons.includes(addonValue)) {
            productConfig.selectedAddons.push(addonValue);
         }
      } else {
         productConfig.selectedAddons = productConfig.selectedAddons.filter(addon => addon !== addonValue);
      }

      console.log('🔧 Current addons:', productConfig.selectedAddons);
      updateTotalPrice();
   }

   /**
    * Increase product quantity
    */
   window.increaseQuantity = function () {
      const currentQty = parseInt(quantityInput.value) || 1;
      const newQty = Math.min(currentQty + 1, 99); // Max 99

      if (quantityInput) {
         quantityInput.value = newQty;
         productConfig.quantity = newQty;
         updateTotalPrice();

         // Visual feedback
         quantityInput.style.transform = 'scale(1.1)';
         setTimeout(() => {
            quantityInput.style.transform = 'scale(1)';
         }, 150);
      }
   };

   /**
    * Decrease product quantity
    */
   window.decreaseQuantity = function () {
      const currentQty = parseInt(quantityInput.value) || 1;
      const newQty = Math.max(currentQty - 1, 1); // Min 1

      if (quantityInput) {
         quantityInput.value = newQty;
         productConfig.quantity = newQty;
         updateTotalPrice();

         // Visual feedback
         quantityInput.style.transform = 'scale(1.1)';
         setTimeout(() => {
            quantityInput.style.transform = 'scale(1)';
         }, 150);
      }
   };

   /**
    * Calculate and update total price
    */
   function updateTotalPrice() {
      let totalPrice = productConfig.basePrice;
      console.log('💰 Base price:', totalPrice);

      // Add size price
      const sizePrice = sizePrices[productConfig.selectedSize] || 0;
      totalPrice += sizePrice;
      console.log('💰 After size (' + productConfig.selectedSize + '):', totalPrice, '(+' + sizePrice + ')');

      // Add addon prices
      productConfig.selectedAddons.forEach(addon => {
         const addonPrice = addonPrices[addon] || 0;
         totalPrice += addonPrice;
         console.log('💰 After addon (' + addon + '):', totalPrice, '(+' + addonPrice + ')');
      });

      // Multiply by quantity
      totalPrice *= productConfig.quantity;
      console.log('💰 Final price (qty=' + productConfig.quantity + '):', totalPrice);

      productConfig.currentPrice = totalPrice;

      // Update display
      if (totalPriceElement) {
         totalPriceElement.textContent = formatPrice(totalPrice);
         console.log('💰 Price display updated:', formatPrice(totalPrice));

         // Add animation
         totalPriceElement.classList.add('product-price-update');
         setTimeout(() => {
            totalPriceElement.classList.remove('product-price-update');
         }, 500);
      } else {
         console.error('❌ Total price element not found!');
      }
   }

   /**
    * Format price for display
    */
   function formatPrice(price) {
      return '$' + parseFloat(price).toFixed(2);
   }

   /**
    * Add product to cart
    */
   window.addToCart = function () {
      console.log('🛒 Add to cart clicked!');

      if (!addToCartBtn) {
         console.error('❌ Add to cart button not found!');
         return;
      }

      // Get product information
      const productData = {
         id: getProductId(),
         name: getProductName(),
         image: mainProductImage ? mainProductImage.src : '/images/burger1.svg',
         basePrice: productConfig.basePrice,
         selectedSize: productConfig.selectedSize,
         selectedAddons: productConfig.selectedAddons,
         quantity: productConfig.quantity,
         totalPrice: productConfig.currentPrice
      };

      console.log('🛒 Product data:', productData);

      // Show loading state
      const originalText = addToCartBtn.innerHTML;
      addToCartBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Adding...';
      addToCartBtn.disabled = true;

      // Simulate API call
      setTimeout(() => {
         try {
            // Add to cart (simulate)
            addToCartSimulation(productData);

            // Show success
            showSuccessMessage('✅ Product added to cart!');

            // Update cart badge
            updateCartBadge();

            // Reset button
            addToCartBtn.innerHTML = originalText;
            addToCartBtn.disabled = false;

            // Success animation
            addToCartBtn.classList.add('btn-success');
            setTimeout(() => {
               addToCartBtn.classList.remove('btn-success');
            }, 1000);

            console.log('✅ Add to cart completed successfully');
         } catch (error) {
            console.error('❌ Add to cart failed:', error);
            showErrorMessage('Error occurred while adding product to cart!');

            // Reset button
            addToCartBtn.innerHTML = originalText;
            addToCartBtn.disabled = false;
         }
      }, 800);
   };

   /**
    * Buy now functionality
    */
   window.buyNow = function () {
      console.log('🚀 Buy now clicked!');

      // Get product information
      const productData = {
         id: getProductId(),
         name: getProductName(),
         image: mainProductImage ? mainProductImage.src : '/images/burger1.svg',
         basePrice: productConfig.basePrice,
         selectedSize: productConfig.selectedSize,
         selectedAddons: productConfig.selectedAddons,
         quantity: productConfig.quantity,
         totalPrice: productConfig.currentPrice
      };

      const buyNowBtn = document.querySelector('button[onclick="buyNow()"]');
      if (!buyNowBtn) {
         console.error('❌ Buy now button not found!');
         return;
      }

      const originalText = buyNowBtn.innerHTML;

      // Show loading state
      buyNowBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Processing...';
      buyNowBtn.disabled = true;

      // Simulate checkout process
      setTimeout(() => {
         try {
            // Add to cart first
            addToCartSimulation(productData);

            // Show checkout alert
            const alertMessage = '🛒 Redirecting to checkout page!\n\n' +
               'Product: ' + productData.name + '\n' +
               'Size: ' + productData.selectedSize + '\n' +
               'Add-ons: ' + (productData.selectedAddons.length > 0 ? productData.selectedAddons.join(', ') : 'None') + '\n' +
               'Quantity: ' + productData.quantity + '\n' +
               'Total: ' + formatPrice(productData.totalPrice);

            alert(alertMessage);

            // Reset button
            buyNowBtn.innerHTML = originalText;
            buyNowBtn.disabled = false;

            // Update cart badge
            updateCartBadge();

            console.log('✅ Buy now completed successfully');
         } catch (error) {
            console.error('❌ Buy now failed:', error);
            // Reset button
            buyNowBtn.innerHTML = originalText;
            buyNowBtn.disabled = false;
         }
      }, 1000);
   };

   /**
    * Toggle wishlist
    */
   window.toggleWishlist = function (event) {
      console.log('❤️ Wishlist toggle clicked!');

      if (!event) {
         console.error('❌ Event object not provided to toggleWishlist');
         return;
      }

      const wishlistBtn = event.target.closest('button');
      if (!wishlistBtn) {
         console.error('❌ Wishlist button not found');
         return;
      }

      const icon = wishlistBtn.querySelector('i');
      if (!icon) {
         console.error('❌ Wishlist icon not found');
         return;
      }

      try {
         if (icon.classList.contains('far')) {
            // Add to wishlist
            icon.classList.remove('far');
            icon.classList.add('fas');
            wishlistBtn.classList.remove('btn-outline-danger');
            wishlistBtn.classList.add('btn-danger');

            // Visual feedback
            wishlistBtn.style.transform = 'scale(0.95)';
            setTimeout(() => {
               wishlistBtn.style.transform = 'scale(1)';
            }, 150);

            showSuccessMessage('💖 Added to wishlist!');
            console.log('✅ Added to wishlist');
         } else {
            // Remove from wishlist
            icon.classList.remove('fas');
            icon.classList.add('far');
            wishlistBtn.classList.remove('btn-danger');
            wishlistBtn.classList.add('btn-outline-danger');

            showSuccessMessage('💔 Removed from wishlist!');
            console.log('✅ Removed from wishlist');
         }
      } catch (error) {
         console.error('❌ Wishlist toggle failed:', error);
      }
   };

   /**
    * Share product
    */
   window.shareProduct = function () {
      console.log('🔗 Share product clicked!');

      try {
         const productName = getProductName();
         const productUrl = window.location.href;

         console.log('🔗 Sharing:', productName, productUrl);

         if (navigator.share) {
            navigator.share({
               title: productName,
               text: 'Check out this delicious product at FastFood App!',
               url: productUrl
            }).then(() => {
               showSuccessMessage('📤 Product shared successfully!');
               console.log('✅ Native share completed');
            }).catch((error) => {
               console.log('Native share failed, falling back to copy:', error);
               copyToClipboard(productUrl);
            });
         } else {
            // Fallback: copy to clipboard
            console.log('Native share not supported, using clipboard');
            copyToClipboard(productUrl);
         }
      } catch (error) {
         console.error('❌ Share product failed:', error);
         showErrorMessage('Unable to share product!');
      }
   };

   function copyToClipboard(text) {
      if (navigator.clipboard) {
         navigator.clipboard.writeText(text).then(() => {
            showSuccessMessage('🔗 Product link copied to clipboard!');
         }).catch(() => {
            fallbackCopyTextToClipboard(text);
         });
      } else {
         fallbackCopyTextToClipboard(text);
      }
   }

   function fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement("textarea");
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();

      try {
         document.execCommand('copy');
         showSuccessMessage('🔗 Product link copied!');
      } catch (err) {
         showInfoMessage('Unable to copy. Link: ' + text);
      }

      document.body.removeChild(textArea);
   }

   /**
    * Quick add to cart for related products
    */
   window.quickAddToCart = function (productId, event) {
      if (!event) event = window.event;

      const button = event.target.closest('button');
      const originalText = button.innerHTML;

      button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
      button.disabled = true;

      setTimeout(() => {
         // Simulate adding product to cart
         const relatedProductData = {
            id: productId,
            name: button.closest('.card').querySelector('.card-title a').textContent ||
               button.closest('.card').querySelector('.card-title').textContent,
            quantity: 1,
            price: parseFloat(button.closest('.card').querySelector('.price').textContent.replace(/[^\d]/g, ''))
         };

         // Add to local storage cart
         let cart = JSON.parse(localStorage.getItem('cart') || '[]');
         const existingIndex = cart.findIndex(item => item.id === productId);

         if (existingIndex > -1) {
            cart[existingIndex].quantity += 1;
         } else {
            cart.push(relatedProductData);
         }

         localStorage.setItem('cart', JSON.stringify(cart));

         showSuccessMessage('✅ Added ' + relatedProductData.name + ' to cart!');
         updateCartBadge();

         button.innerHTML = '<i class="fas fa-check me-1"></i>Added';
         button.classList.remove('btn-primary');
         button.classList.add('btn-success');

         setTimeout(() => {
            button.innerHTML = originalText;
            button.classList.remove('btn-success');
            button.classList.add('btn-primary');
            button.disabled = false;
         }, 2000);
      }, 500);
   };

   /**
    * Write review
    */
   window.writeReview = function () {
      // Show a simple prompt for demo purposes
      const rating = prompt('Rate from 1-5 stars:');
      const comment = prompt('Enter your comment:');

      if (rating && comment && rating >= 1 && rating <= 5) {
         showSuccessMessage('⭐ Thank you for your review! Your review is being processed.');

         // Could add review to DOM here for immediate feedback
         setTimeout(() => {
            showInfoMessage('💬 Review will be displayed after approval.');
         }, 2000);
      } else if (rating !== null && comment !== null) {
         showInfoMessage('⚠️ Please enter a valid rating (1-5 stars) and comment.');
      }
   };

   /**
    * Load more reviews
    */
   window.loadMoreReviews = function (event) {
      if (!event) event = window.event;

      const button = event.target.closest('button');
      const originalText = button.innerHTML;
      button.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Loading...';
      button.disabled = true;

      setTimeout(() => {
         // Simulate loading more reviews
         showInfoMessage('📄 All available reviews have been loaded.');
         button.style.display = 'none';
      }, 1000);
   };

   /**
    * Helper functions
    */
   function getProductId() {
      // Extract from URL or data attribute
      const urlParts = window.location.pathname.split('/');
      return urlParts[urlParts.length - 1] || '1';
   }

   function getProductName() {
      const titleElement = document.querySelector('.product-title');
      return titleElement ? titleElement.textContent : 'Product';
   }

   function addToCartSimulation(productData) {
      // Simulate adding to cart (replace with actual API call)
      let cart = JSON.parse(localStorage.getItem('cart') || '[]');

      // Check if product already exists
      const existingIndex = cart.findIndex(item =>
         item.id === productData.id &&
         item.selectedSize === productData.selectedSize &&
         JSON.stringify(item.selectedAddons) === JSON.stringify(productData.selectedAddons)
      );

      if (existingIndex > -1) {
         cart[existingIndex].quantity += productData.quantity;
      } else {
         cart.push(productData);
      }

      localStorage.setItem('cart', JSON.stringify(cart));
   }

   function updateCartBadge() {
      const cartBadge = document.querySelector('.cart-badge');
      if (cartBadge) {
         let cart = JSON.parse(localStorage.getItem('cart') || '[]');
         const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
         cartBadge.textContent = totalItems;
      }
   }

   function showSuccessMessage(message) {
      showToast(message, 'success');
   }

   function showErrorMessage(message) {
      showToast(message, 'danger');
   }

   function showInfoMessage(message) {
      showToast(message, 'info');
   }

   function showToast(message, type = 'success') {
      // Create toast element
      const toast = document.createElement('div');
      toast.className = `toast align-items-center text-white bg-${type} border-0 position-fixed top-0 end-0 m-3`;
      toast.style.zIndex = '9999';
      toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-${type === 'success' ? 'check-circle' : 'info-circle'} me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;

      document.body.appendChild(toast);

      const bsToast = new bootstrap.Toast(toast);
      bsToast.show();

      // Remove toast after it's hidden
      toast.addEventListener('hidden.bs.toast', () => {
         toast.remove();
      });
   }

   // Initialize cart badge on page load
   updateCartBadge();
});

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
   anchor.addEventListener('click', function (e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute('href'));
      if (target) {
         target.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
         });
      }
   });
});
