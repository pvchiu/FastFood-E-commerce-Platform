// Profile Page JavaScript
document.addEventListener('DOMContentLoaded', function () {
   if (window.location.pathname === '/profile') {
      initializeProfile();
   }
});

function initializeProfile() {
   loadUserProfile();
   loadOrderHistory();
   loadProfileStats();
   initializePasswordChange();
   initializePasswordStrength();
   initializeSettings();
   initializeEditProfile();
}

// Profile data - would normally come from authentication context
function getCurrentUserFromSession() {
   // This would normally extract from Spring Security context
   // For demo, we'll use sample data based on the database
   const sampleUsers = [
      {
         username: 'John',
         fullName: 'John Java',
         email: 'johnjava@bolobala.com',
         phoneNumber: '04534534534',
         address: 'Bolo Bala Street',
         role: 'CUSTOMER',
         createdAt: '2024-01-15T10:30:00'
      },
      {
         username: 'Chicken',
         fullName: 'Bololo Balala',
         email: 'hshs@ctlh.com',
         phoneNumber: '0435353453',
         address: 'Groves Street',
         role: 'MANAGER',
         createdAt: '2024-01-10T08:15:00'
      }
   ];

   // Return first user as demo (in real app, this would be from session)
   return sampleUsers[0];
}

function loadUserProfile() {
   const userData = getCurrentUserFromSession();

   // Update profile display
   document.getElementById('displayFullName').textContent = userData.fullName;
   document.getElementById('displayUsername').textContent = userData.username;
   document.getElementById('displayEmail').textContent = userData.email;
   document.getElementById('displayPhone').textContent = userData.phoneNumber;
   document.getElementById('displayAddress').textContent = userData.address;
   document.getElementById('displayRole').textContent = userData.role;
   document.getElementById('displayMemberSince').textContent = formatDate(userData.createdAt);

   // Update header
   document.getElementById('profileFullName').textContent = userData.fullName;
   document.getElementById('profileUsername').textContent = '@' + userData.username;
   document.getElementById('profileRole').innerHTML = `
        <i class="fas fa-shield-alt me-2"></i>${userData.role}
    `;
}

function loadOrderHistory() {
   // Mock order data based on the database
   const mockOrders = [
      {
         orderNumber: 'ORD-1001',
         totalAmount: 3.00,
         status: 'PENDING',
         createdAt: '2025-01-20T14:30:00',
         deliveryAddress: '123 Main Street',
         items: [
            { name: 'Coca Cola', quantity: 2, price: 1.50 }
         ]
      },
      {
         orderNumber: 'ORD-1012',
         totalAmount: 8.40,
         status: 'COMPLETED',
         createdAt: '2025-01-19T12:15:00',
         deliveryAddress: '864 Ashwood Street',
         items: [
            { name: 'Vanilla Ice Cream', quantity: 3, price: 2.80 }
         ]
      },
      {
         orderNumber: 'ORD-1013',
         totalAmount: 4.80,
         status: 'PENDING',
         createdAt: '2025-01-18T16:45:00',
         deliveryAddress: '753 Poplar Avenue',
         items: [
            { name: 'Honey Butter Fried Chicken', quantity: 1, price: 4.80 }
         ]
      }
   ];

   setTimeout(() => {
      document.getElementById('ordersLoading').style.display = 'none';

      if (mockOrders.length === 0) {
         document.getElementById('emptyOrders').style.display = 'block';
         return;
      }

      const ordersContainer = document.getElementById('ordersContainer');
      ordersContainer.innerHTML = mockOrders.map(order => `
            <div class="order-card">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div>
                        <h6 class="mb-1">${order.orderNumber}</h6>
                        <small class="text-muted">${formatDateTime(order.createdAt)}</small>
                        <div class="mt-1">
                            <small class="text-muted">
                                <i class="fas fa-map-marker-alt me-1"></i>${order.deliveryAddress}
                            </small>
                        </div>
                    </div>
                    <div class="text-end">
                        <div class="h6 mb-1">$${order.totalAmount.toFixed(2)}</div>
                        <span class="order-status status-${order.status.toLowerCase()}">${order.status}</span>
                    </div>
                </div>
                <div class="order-items">
                    ${order.items.map(item => `
                        <div class="d-flex justify-content-between">
                            <span>${item.quantity}x ${item.name}</span>
                            <span>$${(item.quantity * item.price).toFixed(2)}</span>
                        </div>
                    `).join('')}
                </div>
                <div class="mt-3">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewOrderDetail('${order.orderNumber}')">
                        <i class="fas fa-eye me-1"></i>View Details
                    </button>
                    ${order.status === 'PENDING' ? `
                        <button class="btn btn-sm btn-outline-danger ms-2" onclick="cancelOrder('${order.orderNumber}')">
                            <i class="fas fa-times me-1"></i>Cancel
                        </button>
                    ` : ''}
                </div>
            </div>
        `).join('');
   }, 1000);
}

function loadProfileStats() {
   // Calculate stats from mock data
   setTimeout(() => {
      document.getElementById('totalOrders').textContent = '3';
      document.getElementById('totalSpent').textContent = '$16.20';
      document.getElementById('favoriteCategory').textContent = 'Fast Food';
      document.getElementById('memberSince').textContent = '2024';
   }, 300);
}

function initializePasswordChange() {
   const form = document.getElementById('changePasswordForm');
   if (!form) return; // Guard clause if form doesn't exist

   form.addEventListener('submit', function (e) {
      e.preventDefault();

      const currentPassword = document.getElementById('currentPassword').value;
      const newPassword = document.getElementById('newPassword').value;
      const confirmPassword = document.getElementById('confirmPassword').value;

      // Validation
      if (newPassword !== confirmPassword) {
         showNotification('New passwords do not match!', 'error');
         return;
      }

      if (newPassword.length < 8) {
         showNotification('Password must be at least 8 characters long!', 'error');
         return;
      }

      if (!isStrongPassword(newPassword)) {
         showNotification('Password must contain uppercase, lowercase, number, and special character!', 'error');
         return;
      }

      // Mock validation of current password - BE team cần implement API validation
      // TODO: Replace with actual API call to validate current password
      if (currentPassword !== '123456') { // REMOVE THIS - Demo only
         showNotification('Current password is incorrect!', 'error');
         return;
      }

      // Mock API call
      showNotification('Changing password...', 'info');

      setTimeout(() => {
         showNotification('Password changed successfully! Please login again with your new password.', 'success');
         form.reset();

         // Reset password strength indicator
         const strengthMeter = document.getElementById('strengthMeter');
         const strengthText = document.getElementById('strengthText');
         if (strengthMeter && strengthText) {
            strengthMeter.className = 'strength-meter-fill';
            strengthText.textContent = 'Enter a password';
            strengthText.className = 'strength-text';
         }

         // Optionally redirect to login after password change
         setTimeout(() => {
            if (confirm('You need to login again with your new password. Redirect to login page?')) {
               window.location.href = '/logout';
            }
         }, 2000);
      }, 1500);
   });
}

function initializeSettings() {
   const saveBtn = document.getElementById('saveSettingsBtn');
   saveBtn.addEventListener('click', function () {
      const emailNotifications = document.getElementById('emailNotifications').checked;
      const promotionNotifications = document.getElementById('promotionNotifications').checked;

      // Mock save settings to localStorage
      const settings = {
         emailNotifications,
         promotionNotifications,
         savedAt: new Date().toISOString()
      };

      localStorage.setItem('userSettings', JSON.stringify(settings));
      showNotification('Settings saved successfully!', 'success');
   });

   // Load saved settings
   const savedSettings = localStorage.getItem('userSettings');
   if (savedSettings) {
      const settings = JSON.parse(savedSettings);
      document.getElementById('emailNotifications').checked = settings.emailNotifications;
      document.getElementById('promotionNotifications').checked = settings.promotionNotifications;
   }
}

function initializeEditProfile() {
   const editBtn = document.getElementById('editProfileBtn');
   editBtn.addEventListener('click', function () {
      // Create modal for editing profile
      const modalHtml = `
            <div class="modal fade" id="editProfileModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Edit Profile</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <form id="editProfileForm">
                                <div class="mb-3">
                                    <label for="editFullName" class="form-label">Full Name</label>
                                    <input type="text" class="form-control" id="editFullName" required>
                                </div>
                                <div class="mb-3">
                                    <label for="editEmail" class="form-label">Email</label>
                                    <input type="email" class="form-control" id="editEmail" required>
                                </div>
                                <div class="mb-3">
                                    <label for="editPhone" class="form-label">Phone Number</label>
                                    <input type="tel" class="form-control" id="editPhone">
                                </div>
                                <div class="mb-3">
                                    <label for="editAddress" class="form-label">Address</label>
                                    <textarea class="form-control" id="editAddress" rows="3"></textarea>
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-primary" onclick="saveProfileChanges()">Save Changes</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

      // Remove existing modal if any
      const existingModal = document.getElementById('editProfileModal');
      if (existingModal) {
         existingModal.remove();
      }

      // Add modal to DOM
      document.body.insertAdjacentHTML('beforeend', modalHtml);

      // Populate form with current data
      const userData = getCurrentUserFromSession();
      document.getElementById('editFullName').value = userData.fullName;
      document.getElementById('editEmail').value = userData.email;
      document.getElementById('editPhone').value = userData.phoneNumber;
      document.getElementById('editAddress').value = userData.address;

      // Show modal
      const modal = new bootstrap.Modal(document.getElementById('editProfileModal'));
      modal.show();
   });
}

function saveProfileChanges() {
   const fullName = document.getElementById('editFullName').value;
   const email = document.getElementById('editEmail').value;
   const phone = document.getElementById('editPhone').value;
   const address = document.getElementById('editAddress').value;

   // Validation
   if (!fullName || !email) {
      showNotification('Full name and email are required!', 'error');
      return;
   }

   // Mock API call to save changes
   showNotification('Saving changes...', 'info');

   setTimeout(() => {
      // Update display
      document.getElementById('displayFullName').textContent = fullName;
      document.getElementById('displayEmail').textContent = email;
      document.getElementById('displayPhone').textContent = phone;
      document.getElementById('displayAddress').textContent = address;
      document.getElementById('profileFullName').textContent = fullName;

      // Close modal
      const modal = bootstrap.Modal.getInstance(document.getElementById('editProfileModal'));
      modal.hide();

      showNotification('Profile updated successfully!', 'success');
   }, 1000);
}

function viewOrderDetail(orderNumber) {
   showNotification(`Loading details for ${orderNumber}...`, 'info');
   // In real app, this would navigate to order detail page
   setTimeout(() => {
      showNotification(`Order ${orderNumber} details would be shown here`, 'info');
   }, 500);
}

function cancelOrder(orderNumber) {
   if (confirm(`Are you sure you want to cancel order ${orderNumber}?`)) {
      showNotification('Cancelling order...', 'info');

      setTimeout(() => {
         showNotification(`Order ${orderNumber} has been cancelled`, 'success');
         // Reload order history
         loadOrderHistory();
      }, 1000);
   }
}

function formatDate(dateString) {
   return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
   });
}

function formatDateTime(dateString) {
   return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
   });
}

function showNotification(message, type = 'info') {
   const notification = document.createElement('div');
   notification.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show position-fixed`;
   notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';

   notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

   document.body.appendChild(notification);

   setTimeout(() => {
      if (notification.parentNode) {
         notification.remove();
      }
   }, 3000);
}

// Password Strength Functions
function initializePasswordStrength() {
   const newPasswordInput = document.getElementById('newPassword');
   const confirmPasswordInput = document.getElementById('confirmPassword');

   if (newPasswordInput) {
      newPasswordInput.addEventListener('input', function () {
         checkPasswordStrength(this.value);
      });
   }

   if (confirmPasswordInput) {
      confirmPasswordInput.addEventListener('input', function () {
         checkPasswordMatch();
      });
   }
}

function checkPasswordStrength(password) {
   const strengthMeter = document.getElementById('strengthMeter');
   const strengthText = document.getElementById('strengthText');

   if (!strengthMeter || !strengthText) return;

   const strength = calculatePasswordStrength(password);

   // Remove all strength classes
   strengthMeter.className = 'strength-meter-fill';
   strengthText.className = 'strength-text';

   if (password.length === 0) {
      strengthText.textContent = 'Enter a password';
      return;
   }

   // Add appropriate strength class
   switch (strength.level) {
      case 1:
         strengthMeter.classList.add('strength-weak');
         strengthText.classList.add('text-weak');
         strengthText.textContent = 'Weak password';
         break;
      case 2:
         strengthMeter.classList.add('strength-fair');
         strengthText.classList.add('text-fair');
         strengthText.textContent = 'Fair password';
         break;
      case 3:
         strengthMeter.classList.add('strength-good');
         strengthText.classList.add('text-good');
         strengthText.textContent = 'Good password';
         break;
      case 4:
         strengthMeter.classList.add('strength-strong');
         strengthText.classList.add('text-strong');
         strengthText.textContent = 'Strong password';
         break;
      default:
         strengthText.textContent = 'Very weak password';
   }
}

function calculatePasswordStrength(password) {
   let score = 0;
   const checks = {
      length: password.length >= 8,
      lowercase: /[a-z]/.test(password),
      uppercase: /[A-Z]/.test(password),
      numbers: /\d/.test(password),
      special: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password)
   };

   // Length check
   if (checks.length) score++;
   if (password.length >= 12) score++;

   // Character type checks
   if (checks.lowercase) score++;
   if (checks.uppercase) score++;
   if (checks.numbers) score++;
   if (checks.special) score++;

   return {
      score: score,
      level: Math.min(Math.floor(score / 1.5), 4),
      checks: checks
   };
}

function isStrongPassword(password) {
   const strength = calculatePasswordStrength(password);
   return strength.level >= 3; // Good or Strong
}

function checkPasswordMatch() {
   const newPassword = document.getElementById('newPassword').value;
   const confirmPassword = document.getElementById('confirmPassword').value;
   const matchIndicator = document.getElementById('passwordMatch');

   if (!matchIndicator) return;

   if (confirmPassword.length === 0) {
      matchIndicator.style.display = 'none';
      return;
   }

   if (newPassword === confirmPassword) {
      matchIndicator.style.display = 'none';
   } else {
      matchIndicator.style.display = 'block';
   }
}

function togglePassword(inputId) {
   const input = document.getElementById(inputId);
   const toggle = document.getElementById(inputId + 'Toggle');

   if (!input || !toggle) return;

   if (input.type === 'password') {
      input.type = 'text';
      toggle.className = 'fas fa-eye-slash';
   } else {
      input.type = 'password';
      toggle.className = 'fas fa-eye';
   }
}
