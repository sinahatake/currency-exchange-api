package org.example.service;

import org.example.mapperDto.CurrencyMapper;
import org.example.dao.CurrencyDAO;
import org.example.dto.CurrencyDTO;
import org.example.entity.Currency;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CurrencyService {
    private final CurrencyDAO currencyDAO;
    private final CurrencyMapper currencyMapper;

    public CurrencyService(
            CurrencyDAO currencyDAO,
            CurrencyMapper currencyMapper) {
        this.currencyDAO = currencyDAO;
        this.currencyMapper = currencyMapper;
    }


    public List<CurrencyDTO> getAllCurrencies() throws SQLException {
        List<CurrencyDTO> currencies = new ArrayList<>();
        List<Currency> currencyDAOAll = currencyDAO.findAll();
        for (Currency currency : currencyDAOAll) {
            CurrencyDTO currencyDTO = currencyMapper.toDto(currency);
            currencies.add(currencyDTO);
        }
        return currencies;
    }

    public CurrencyDTO findByCode(String code) throws SQLException {
        if (code == null || code.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }
        return currencyDAO.findByCode(code)
                .map(currencyMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Currency with code " + code + " not found"));
    }

    public CurrencyDTO addNewCurrency(String code, String name, String sign) throws SQLException {
        if (code == null || code.isBlank() ||
            name == null || name.isBlank() ||
            sign == null || sign.isBlank()) {
            throw new InvalidParameterException("Missing form fields: all fields (code, name, sign) are required");
        }

        if (code.trim().length() != 3) {
            throw new InvalidParameterException("Invalid currency code: must be exactly 3 characters");
        }

        String formattedCode = code.trim().toUpperCase();

        if (currencyDAO.findByCode(formattedCode).isPresent()) {
            throw new AlreadyExistsException("Currency with code " + formattedCode + " already exists");
        }

        Currency currency = new Currency();
        currency.setCode(code);
        currency.setFullName(name);
        currency.setSign(sign);

        Currency saved = currencyDAO.save(currency);
        return currencyMapper.toDto(saved);
    }

}
