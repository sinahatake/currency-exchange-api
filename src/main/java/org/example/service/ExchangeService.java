package org.example.service;


import org.example.dto.ExchangeResultDto;

import java.math.BigDecimal;

public interface ExchangeService {
    ExchangeResultDto exchange(String baseCode, String targetCode, BigDecimal amount);
}