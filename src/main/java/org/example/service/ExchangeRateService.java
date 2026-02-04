package org.example.service;

import org.example.dto.ExchangeRateDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    List<ExchangeRateDTO> getAllExchangeRates();

    ExchangeRateDTO getExchangeRateByCodes(String baseCode, String targetCode);

    ExchangeRateDTO addExchangeRate(String baseCode, String targetCode, BigDecimal rate);

    ExchangeRateDTO updateExchangeRate(String baseCode, String targetCode, BigDecimal rate);

    Optional<ExchangeRateDTO> findByCodes(String baseCode, String targetCode);
}