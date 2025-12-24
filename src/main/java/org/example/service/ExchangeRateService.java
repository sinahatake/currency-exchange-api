package org.example.service;

import org.example.MapperDTO.ExchangeRateMapper;
import org.example.dao.ExchangeRateDAO;
import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.entity.ExchangeRate;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private final ExchangeRateDAO exchangeRateDAO;
    private final ExchangeRateMapper exchangeRateMapper;
    private final CurrencyService currencyService;

    public ExchangeRateService(
            ExchangeRateDAO exchangeRateDAO,
            ExchangeRateMapper exchangeRateMapper,
            CurrencyService currencyService) {
        this.exchangeRateDAO = exchangeRateDAO;
        this.exchangeRateMapper = exchangeRateMapper;
        this.currencyService = currencyService;
    }


    public List<ExchangeRateDTO> getAllExchangeRates() throws SQLException {
        List<ExchangeRateDTO> exchangeRates = new ArrayList<>();
        List<ExchangeRate> exchangeRatesDao = exchangeRateDAO.findAll();
        for (ExchangeRate exchangeRate : exchangeRatesDao) {
            ExchangeRateDTO exchangeRateDTO = exchangeRateMapper.toDto(exchangeRate);
            exchangeRates.add(exchangeRateDTO);
        }
        return exchangeRates;
    }

    public ExchangeRateDTO getExchangeRateByCodes(String baseCode, String targetCode) throws SQLException {
        return exchangeRateDAO.findByCurrencyCodes(baseCode, targetCode)
                .map(exchangeRateMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Exchange rate with codes " + baseCode + targetCode + " not found"));
    }

    public ExchangeRateDTO addExchangeRate(String baseCode, String targetCode, BigDecimal rate) throws SQLException {
        CurrencyDTO baseCurrency = currencyService.findByCode(baseCode);
        CurrencyDTO targetCurrency = currencyService.findByCode(targetCode);

        if (baseCode == null || baseCode.length() != 3 || targetCode == null || targetCode.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }

        if (exchangeRateDAO.findByCurrencyCodes(baseCode, targetCode).isPresent()) {
            throw new AlreadyExistsException("Exchange rate for pair " + baseCode + " -> " + targetCode + " already exists");
        }

        ExchangeRateDTO exchangeRate = new ExchangeRateDTO();
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(rate);

        ExchangeRate saved = exchangeRateDAO.save(exchangeRateMapper.toEntity(exchangeRate));
        return exchangeRateMapper.toDto(saved);
    }

    public ExchangeRateDTO updateExchangeRate(String baseCode, String targetCode, BigDecimal rate) throws SQLException {
        CurrencyDTO baseCurrency = currencyService.findByCode(baseCode);
        CurrencyDTO targetCurrency = currencyService.findByCode(targetCode);

        ExchangeRateDTO exchangeRate = new ExchangeRateDTO();
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(rate.setScale(2, RoundingMode.HALF_UP));
        boolean isUpdated = exchangeRateDAO.update(exchangeRateMapper.toEntity(exchangeRate));
        if (!isUpdated) {
            throw new EntityNotFoundException("Exchange rate with codes " + baseCode + targetCode + " not found");
        }

        return exchangeRate;
    }

    public Optional<ExchangeRateDTO> findByCodes(String baseCode, String targetCode) throws SQLException {
        return exchangeRateDAO.findByCurrencyCodes(baseCode, targetCode)
                .map(exchangeRateMapper::toDto);
    }

}
