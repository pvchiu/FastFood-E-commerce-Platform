package com.example.myproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    @Value("${vnpay.tmncode}")
    private String tmnCode;
    
    @Value("${vnpay.hashsecret}")
    private String hashSecret;
    
    @Value("${vnpay.url}")
    private String vnpUrl;
    
    @Value("${vnpay.returnurl}")
    private String returnUrl;
    
    public String getTmnCode() { return tmnCode; }
    public String getHashSecret() { return hashSecret; }
    public String getVnpUrl() { return vnpUrl; }
    public String getReturnUrl() { return returnUrl; }
}