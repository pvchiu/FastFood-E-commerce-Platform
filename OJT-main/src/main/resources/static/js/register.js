/**
 * Registration Page JavaScript
 * Handles form validation, password strength, and user interactions
 */

document.addEventListener("DOMContentLoaded", function () {
  // Form elements
  const form = document.getElementById("registerForm");
  const passwordInput = document.getElementById("password");
  const confirmPasswordInput = document.getElementById("confirmPassword");
  const togglePasswordBtns = document.querySelectorAll('[id^="toggle"]');
  const strengthIndicator = document.querySelector(".password-strength");
  const submitBtn = form.querySelector('button[type="submit"]');
  const loadingBtn = document.getElementById("loadingButton");

  // Initialize form validation
  initializeFormValidation();

  // Initialize password features
  initializePasswordFeatures();

  // Initialize real-time validation
  initializeRealTimeValidation();

  /**
   * Initialize Bootstrap form validation
   */
  function initializeFormValidation() {
    form.addEventListener("submit", function (event) {
      event.preventDefault();
      event.stopPropagation();

      if (validateForm()) {
        form.submit();
      }

      form.classList.add("was-validated");
    });
  }

  /**
   * Validate entire form
   */
  function validateForm() {
    let isValid = true;

    // Check all required fields
    const requiredFields = form.querySelectorAll("[required]");
    requiredFields.forEach((field) => {
      if (!field.checkValidity()) {
        isValid = false;
      }
    });

    // Custom validations
    if (!validatePasswordMatch()) isValid = false;
    if (!validatePasswordStrength()) isValid = false;
    if (!validateUsername()) isValid = false;
    if (!validatePhoneNumber()) isValid = false;

    return isValid;
  }

  /**
   * Initialize password-related features
   */
  function initializePasswordFeatures() {
    // Password visibility toggle
    togglePasswordBtns.forEach((btn) => {
      btn.addEventListener("click", function () {
        const targetId = this.id.replace("toggle", "").toLowerCase();
        const targetInput = document.getElementById(
          targetId === "password" ? "password" : "confirmPassword"
        );
        const icon = this.querySelector("i");

        if (targetInput.type === "password") {
          targetInput.type = "text";
          icon.className = "fas fa-eye-slash";
        } else {
          targetInput.type = "password";
          icon.className = "fas fa-eye";
        }
      });
    });

    // Password strength indicator
    passwordInput.addEventListener("input", function () {
      const strength = calculatePasswordStrength(this.value);
      updatePasswordStrength(strength);

      if (this.value.length > 0) {
        strengthIndicator.style.display = "block";
      } else {
        strengthIndicator.style.display = "none";
      }
    });

    // Confirm password validation
    confirmPasswordInput.addEventListener("input", validatePasswordMatch);
  }

  /**
   * Calculate password strength
   */
  function calculatePasswordStrength(password) {
    let score = 0;
    const checks = {
      length: password.length >= 8,
      lowercase: /[a-z]/.test(password),
      uppercase: /[A-Z]/.test(password),
      numbers: /\d/.test(password),
      symbols: /[^A-Za-z0-9]/.test(password),
    };

    // Calculate score
    Object.values(checks).forEach((check) => {
      if (check) score++;
    });

    // Determine strength level
    if (score === 0) return { level: 0, text: "Very weak", class: "bg-danger" };
    if (score <= 2) return { level: 20, text: "Weak", class: "bg-danger" };
    if (score === 3) return { level: 50, text: "Average", class: "bg-warning" };
    if (score === 4) return { level: 75, text: "Strong", class: "bg-info" };
    if (score === 5)
      return { level: 100, text: "Very strong", class: "bg-success" };

    return { level: 20, text: "Weak", class: "bg-danger" };
  }

  /**
   * Update password strength UI
   */
  function updatePasswordStrength(strength) {
    const progressBar = strengthIndicator.querySelector(".progress-bar");
    const strengthText = document.getElementById("strengthText");

    progressBar.style.width = strength.level + "%";
    progressBar.className = `progress-bar ${strength.class}`;
    strengthText.textContent = strength.text;
  }

  /**
   * Reset password strength indicator
   */
  function resetPasswordStrength() {
    strengthIndicator.style.display = "none";
    const progressBar = strengthIndicator.querySelector(".progress-bar");
    progressBar.style.width = "0%";
    progressBar.className = "progress-bar";
    document.getElementById("strengthText").textContent = "Weak";
  }

  /**
   * Validate password match
   */
  function validatePasswordMatch() {
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (confirmPassword.length > 0 && password !== confirmPassword) {
      confirmPasswordInput.setCustomValidity(
        "Password confirmation does not match"
      );
      return false;
    } else {
      confirmPasswordInput.setCustomValidity("");
      return true;
    }
  }

  /**
   * Validate password strength requirement
   */
  function validatePasswordStrength() {
    const password = passwordInput.value;
    const strength = calculatePasswordStrength(password);

    if (password.length > 0 && strength.level < 50) {
      passwordInput.setCustomValidity(
        "Password is too weak. Please use a stronger password."
      );
      return false;
    } else {
      passwordInput.setCustomValidity("");
      return true;
    }
  }

  /**
   * Validate username format
   */
  function validateUsername() {
    const username = document.getElementById("username").value;
    const pattern = /^[a-zA-Z0-9_]{3,50}$/;

    if (username.length > 0 && !pattern.test(username)) {
      document
        .getElementById("username")
        .setCustomValidity(
          "Username can only contain letters, numbers, and underscores"
        );
      return false;
    } else {
      document.getElementById("username").setCustomValidity("");
      return true;
    }
  }

  /**
   * Validate phone number format
   */
  function validatePhoneNumber() {
    const phone = document.getElementById("phoneNumber").value;
    const pattern = /^[0-9]{10}$/;

    if (phone.length > 0 && !pattern.test(phone)) {
      document
        .getElementById("phoneNumber")
        .setCustomValidity("Phone number must be 10 digits");
      return false;
    } else {
      document.getElementById("phoneNumber").setCustomValidity("");
      return true;
    }
  }

  /**
   * Initialize real-time validation
   */
  document.getElementById("username").addEventListener("blur", function () {
    const username = this.value;
    const pattern = /^[a-zA-Z0-9_]{3,50}$/;

    if (!pattern.test(username)) {
      this.setCustomValidity(
        "Username can only contain letters, numbers, and underscores"
      );
      showFieldError(
        this,
        "Username can only contain letters, numbers, and underscores"
      );
      updateFieldValidation(this);
      return;
    }

    // Nếu định dạng hợp lệ thì mới kiểm tra trùng
    fetch(`/api/check-username?username=${encodeURIComponent(username)}`)
      .then((res) => res.json())
      .then((exists) => {
        if (exists) {
          this.setCustomValidity("Username is already taken.");
          showFieldError(this, "Username is already taken.");
        } else {
          this.setCustomValidity("");
          clearFieldError(this);
        }
        updateFieldValidation(this);
      });
  });
  document.getElementById("email").addEventListener("blur", function () {
    const email = this.value;

    if (!email.includes("@")) {
      this.setCustomValidity("Invalid email format.");
      showFieldError(this, "Invalid email format.");
      updateFieldValidation(this);
      return;
    }

    fetch(`/api/check-email?email=${encodeURIComponent(email)}`)
      .then((res) => res.json())
      .then((exists) => {
        if (exists) {
          this.setCustomValidity("Email is already in use.");
          showFieldError(this, "Email is already in use.");
        } else {
          this.setCustomValidity("");
          clearFieldError(this);
        }
        updateFieldValidation(this);
      });
  });

  /**
   * Update field validation UI
   */
  function updateFieldValidation(field) {
    if (field.checkValidity()) {
      field.classList.remove("is-invalid");
      field.classList.add("is-valid");
    } else {
      field.classList.remove("is-valid");
      field.classList.add("is-invalid");
    }
  }

  /**
   * Show loading state
   */
  function showLoading() {
    submitBtn.style.display = "none";
    loadingBtn.style.display = "block";

    // Disable form inputs
    const inputs = form.querySelectorAll("input, textarea, button");
    inputs.forEach((input) => (input.disabled = true));
  }

  /**
   * Hide loading state
   */
  function hideLoading() {
    submitBtn.style.display = "block";
    loadingBtn.style.display = "none";

    // Re-enable form inputs
    const inputs = form.querySelectorAll("input, textarea, button");
    inputs.forEach((input) => (input.disabled = false));
  }

  /**
   * Show success message
   */
  function showSuccessMessage(message) {
    // Remove any existing alerts
    const existingAlerts = document.querySelectorAll(".alert");
    existingAlerts.forEach((alert) => alert.remove());

    // Create success alert
    const alert = document.createElement("div");
    alert.className = "alert alert-success alert-dismissible fade show";
    alert.innerHTML = `
            <i class="fas fa-check-circle me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

    // Insert alert
    const cardBody = document.querySelector(".card-body");
    cardBody.insertBefore(alert, cardBody.firstChild);

    // Auto-hide after 5 seconds
    setTimeout(() => {
      alert.remove();
    }, 5000);

    // Scroll to top
    document.querySelector(".card").scrollIntoView({ behavior: "smooth" });
  }

  /**
   * Show error message
   */
  function showErrorMessage(message) {
    // Remove any existing alerts
    const existingAlerts = document.querySelectorAll(".alert");
    existingAlerts.forEach((alert) => alert.remove());

    // Create error alert
    const alert = document.createElement("div");
    alert.className = "alert alert-danger alert-dismissible fade show";
    alert.innerHTML = `
            <i class="fas fa-exclamation-circle me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

    // Insert alert
    const cardBody = document.querySelector(".card-body");
    cardBody.insertBefore(alert, cardBody.firstChild);

    // Scroll to top
    document.querySelector(".card").scrollIntoView({ behavior: "smooth" });
  }

  /**
   * Format phone number input
   */
  document
    .getElementById("phoneNumber")
    .addEventListener("input", function (e) {
      // Remove all non-digits
      let value = e.target.value.replace(/\D/g, "");

      // Limit to 11 digits
      if (value.length > 11) {
        value = value.slice(0, 11);
      }

      e.target.value = value;
    });

  /**
   * Format full name input (capitalize first letters)
   */
  document.getElementById("fullName").addEventListener("blur", function (e) {
    const value = e.target.value;
    if (value) {
      // Capitalize first letter of each word
      const formatted = value
        .toLowerCase()
        .replace(/\b\w/g, (l) => l.toUpperCase());
      e.target.value = formatted;
    }
  });

  function showFieldError(input, message) {
    let error = input.nextElementSibling;
    if (!error || !error.classList.contains("invalid-feedback")) {
      error = document.createElement("div");
      error.className = "invalid-feedback";
      input.insertAdjacentElement("afterend", error);
    }
    error.textContent = message;
  }

  function clearFieldError(input) {
    let error = input.nextElementSibling;
    if (error && error.classList.contains("invalid-feedback")) {
      error.remove();
    }
  }
});
