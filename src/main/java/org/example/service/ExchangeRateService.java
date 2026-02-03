package org.example.service;

import org.example.dao.CurrencyDao;
import org.example.dao.ExchangeRateDao;
import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.entity.ExchangeRate;
import org.example.exceptions.DatabaseException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.example.mapper.ExchangeRateMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private final ExchangeRateDao ExchangeRateDao;
    private final CurrencyDao CurrencyDao;
    private final ExchangeRateMapper exchangeRateMapper;
    private final CurrencyService currencyService;

    public ExchangeRateService(
            ExchangeRateDao ExchangeRateDao, CurrencyDao CurrencyDao,
            ExchangeRateMapper exchangeRateMapper,
            CurrencyService currencyService) {
        this.ExchangeRateDao = ExchangeRateDao;
        this.CurrencyDao = CurrencyDao;
        this.exchangeRateMapper = exchangeRateMapper;
        this.currencyService = currencyService;
    }


    public List<ExchangeRateDTO> getAllExchangeRates() {
        List<ExchangeRateDTO> exchangeRates = new ArrayList<>();
        List<ExchangeRate> exchangeRatesDao = ExchangeRateDao.findAll();
        for (ExchangeRate exchangeRate : exchangeRatesDao) {
            ExchangeRateDTO exchangeRateDTO = exchangeRateMapper.toDto(exchangeRate);
            exchangeRates.add(exchangeRateDTO);
        }
        return exchangeRates;
    }

    public ExchangeRateDTO getExchangeRateByCodes(String baseCode, String targetCode) {
        if (baseCode == null || baseCode.length() != 3 || targetCode == null || targetCode.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }
        return ExchangeRateDao.findByCurrencyCodes(baseCode, targetCode)
                .map(exchangeRateMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Exchange rate with codes " + baseCode + targetCode + " not found"));
    }

    public ExchangeRateDTO addExchangeRate(String baseCode, String targetCode, BigDecimal rate) {
        CurrencyDTO baseCurrency = currencyService.findByCode(baseCode);
        CurrencyDTO targetCurrency = currencyService.findByCode(targetCode);

        if (baseCode == null || baseCode.length() != 3 || targetCode == null || targetCode.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }

        if (CurrencyDao.findByCode(targetCode).isEmpty()) {
            throw new EntityNotFoundException("Currency " + targetCode + " is not exists");
        }

        if (CurrencyDao.findByCode(baseCode).isEmpty()) {
            throw new EntityNotFoundException("Currency " + baseCode + " is not exists");
        }

        ExchangeRateDTO exchangeRate = new ExchangeRateDTO();
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(rate);
        ExchangeRate saved = ExchangeRateDao.save(exchangeRateMapper.toEntity(exchangeRate));
        return exchangeRateMapper.toDto(saved);

    }

    public ExchangeRateDTO updateExchangeRate(String baseCode, String targetCode, BigDecimal rate) {
        if (baseCode == null || baseCode.length() != 3 || targetCode == null || targetCode.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }

        ExchangeRate existingEntity = ExchangeRateDao.findByCurrencyCodes(baseCode, targetCode)
                .orElseThrow(() -> new EntityNotFoundException("Exchange rate with codes " + baseCode + targetCode + " not found"));

        existingEntity.setRate(rate);

        boolean isUpdated = ExchangeRateDao.update(existingEntity);

        if (!isUpdated) {
            throw new DatabaseException("Failed to update exchange rate in database");
        }

        return exchangeRateMapper.toDto(existingEntity);
    }

    public Optional<ExchangeRateDTO> findByCodes(String baseCode, String targetCode) {
        return ExchangeRateDao.findByCurrencyCodes(baseCode, targetCode)
                .map(exchangeRateMapper::toDto);
    }

}
