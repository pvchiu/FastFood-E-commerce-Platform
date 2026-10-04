package com.example.myproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyService {
    
    @Value("${currency.usd-to-vnd-rate:24000}")
    private BigDecimal usdToVndRate;
    
    public BigDecimal convertUsdToVnd(BigDecimal usdAmount) {
        if (usdAmount == null) {
            return BigDecimal.ZERO;
        }
        return usdAmount.multiply(usdToVndRate).setScale(0, RoundingMode.HALF_UP);
    }
    
    public BigDecimal convertVndToUsd(BigDecimal vndAmount) {
        if (vndAmount == null) {
            return BigDecimal.ZERO;
        }
        return vndAmount.divide(usdToVndRate, 2, RoundingMode.HALF_UP);
    }
    
    public String formatVnd(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).toString() + " VND";
    }

    public String formatUsd(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString() + " USD";
    }
}