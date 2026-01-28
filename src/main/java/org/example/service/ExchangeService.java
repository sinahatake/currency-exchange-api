package org.example.service;

import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.dto.ExchangeResultDTO;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

public class ExchangeService {
    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;

    public ExchangeService(CurrencyService currencyService, ExchangeRateService exchangeRateService) {
        this.currencyService = currencyService;
        this.exchangeRateService = exchangeRateService;
    }

    public ExchangeResultDTO exchange(String baseCode, String targetCode, BigDecimal amount) throws SQLException {
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
        result.setRate(rate);
        result.setConvertedAmount(amount.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    BigDecimal calculateRate(String baseCode, String targetCode) throws SQLException {

        Optional<ExchangeRateDTO> directRate = exchangeRateService.findByCodes(baseCode, targetCode);
        if (directRate.isPresent()) {
            return directRate.get().getRate();
        }

        Optional<ExchangeRateDTO> reverseRate = exchangeRateService.findByCodes(targetCode, baseCode);
        if (reverseRate.isPresent()) {
            return BigDecimal.ONE.divide(reverseRate.get().getRate(), 2, RoundingMode.HALF_UP);
        }

        Optional<ExchangeRateDTO> UsdToBase = exchangeRateService.findByCodes("USD", baseCode);
        Optional<ExchangeRateDTO> UsdToTarget = exchangeRateService.findByCodes("USD", targetCode);
        if (UsdToBase.isPresent() && UsdToTarget.isPresent()) {
            BigDecimal UsdToBaseRate = UsdToBase.get().getRate();
            BigDecimal UsdToTargetRate = UsdToTarget.get().getRate();
            return UsdToTargetRate.divide(UsdToBaseRate, 2, RoundingMode.HALF_UP);
        }

        throw new EntityNotFoundException("Exchange rate not found");

    }


}
