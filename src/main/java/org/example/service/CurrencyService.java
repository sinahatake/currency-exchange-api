package org.example.service;

import org.example.dao.CurrencyDao;
import org.example.dto.CurrencyDTO;
import org.example.entity.Currency;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.example.mapper.CurrencyMapper;

import java.util.ArrayList;
import java.util.List;

public class CurrencyService {
    private final CurrencyDao CurrencyDao;
    private final CurrencyMapper currencyMapper;

    public CurrencyService(
            CurrencyDao CurrencyDao,
            CurrencyMapper currencyMapper) {
        this.CurrencyDao = CurrencyDao;
        this.currencyMapper = currencyMapper;
    }


    public List<CurrencyDTO> getAllCurrencies() {
        List<CurrencyDTO> currencies = new ArrayList<>();
        List<Currency> CurrencyDaoAll = CurrencyDao.findAll();
        for (Currency currency : CurrencyDaoAll) {
            CurrencyDTO currencyDTO = currencyMapper.toDto(currency);
            currencies.add(currencyDTO);
        }
        return currencies;
    }

    public CurrencyDTO findByCode(String code) {
        if (code == null || code.length() != 3) {
            throw new InvalidParameterException("Invalid currency code");
        }
        return CurrencyDao.findByCode(code)
                .map(currencyMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Currency with code " + code + " not found"));
    }

    public CurrencyDTO addNewCurrency(String code, String name, String sign) {
        if (code == null || code.isBlank() ||
            name == null || name.isBlank() ||
            sign == null || sign.isBlank()) {
            throw new InvalidParameterException("Missing form fields: all fields (code, name, sign) are required");
        }

        if (code.trim().length() != 3) {
            throw new InvalidParameterException("Invalid currency code: must be exactly 3 characters");
        }

        String formattedCode = code.trim().toUpperCase();

        Currency currency = new Currency();
        currency.setCode(formattedCode);
        currency.setFullName(name);
        currency.setSign(sign);
        Currency saved = CurrencyDao.save(currency);
        return currencyMapper.toDto(saved);

    }

}
