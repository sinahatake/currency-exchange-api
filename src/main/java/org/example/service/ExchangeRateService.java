package org.example.service;

import org.example.dto.ExchangeRateDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    List<ExchangeRateDto> getAllExchangeRates();

    ExchangeRateDto getExchangeRateByCodes(String baseCode, String targetCode);

    ExchangeRateDto addExchangeRate(String baseCode, String targetCode, BigDecimal rate);

    ExchangeRateDto updateExchangeRate(String baseCode, String targetCode, BigDecimal rate);

    Optional<ExchangeRateDto> findByCodes(String baseCode, String targetCode);
}