package org.example.service;

import org.example.dto.CurrencyDto;

import java.util.List;

public interface CurrencyService {
    List<CurrencyDto> getAllCurrencies();

    CurrencyDto findByCode(String code);

    CurrencyDto addNewCurrency(String code, String name, String sign);
}