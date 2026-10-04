package com.example.myproject.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {      
        String errorMessage;
        if (exception instanceof DisabledException || 
            (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("inactive"))) {
            errorMessage = "Your account has been deactivated. Please contact administrator.";
            System.out.println("User account is disabled");
        } else {
            errorMessage = "Incorrect username or password!";
            System.out.println("Invalid credentials");
        }
        
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        String redirectUrl = "/login?error=true&message=" + encodedMessage;
        System.out.println("Redirecting to: " + redirectUrl);
        
        response.sendRedirect(redirectUrl);
    }
}