package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.dto.ExchangeResultDTO;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@RequiredArgsConstructor
public class ExchangeService {
    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;

    public ExchangeResultDTO exchange(String baseCode, String targetCode, BigDecimal amount) {
        if (baseCode == null || baseCode.length() != 3 || targetCode == null || targetCode.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }

        ExchangeResultDTO result = new ExchangeResultDTO();

        CurrencyDTO baseCurrency = currencyService.findByCode(baseCode);
        CurrencyDTO targetCurrency = currencyService.findByCode(targetCode);

        result.setBaseCurrency(baseCurrency);
        result.setTargetCurrency(targetCurrency);
        result.setAmount(amount);

        BigDecimal rate = calculateRate(baseCode, targetCode);

        result.setRate(rate.setScale(6, RoundingMode.HALF_UP));
        result.setConvertedAmount(amount.multiply(rate).setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    private BigDecimal calculateRate(String baseCode, String targetCode) {
        Optional<ExchangeRateDTO> directRate = exchangeRateService.findByCodes(baseCode, targetCode);
        if (directRate.isPresent()) {
            return directRate.get().getRate();
        }

        Optional<ExchangeRateDTO> reverseRate = exchangeRateService.findByCodes(targetCode, baseCode);
        if (reverseRate.isPresent()) {
            return BigDecimal.ONE.divide(reverseRate.get().getRate(), 10, RoundingMode.HALF_UP);
        }

        Optional<ExchangeRateDTO> usdToBase = exchangeRateService.findByCodes("USD", baseCode);
        Optional<ExchangeRateDTO> usdToTarget = exchangeRateService.findByCodes("USD", targetCode);

        if (usdToBase.isPresent() && usdToTarget.isPresent()) {
            BigDecimal usdToBaseRate = usdToBase.get().getRate();
            BigDecimal usdToTargetRate = usdToTarget.get().getRate();
            return usdToTargetRate.divide(usdToBaseRate, 10, RoundingMode.HALF_UP);
        }

        throw new EntityNotFoundException("Exchange rate not found");
    }
}