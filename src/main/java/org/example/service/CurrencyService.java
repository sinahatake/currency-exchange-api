package org.example.service;

import org.example.dto.CurrencyDTO;

import java.util.List;

public interface CurrencyService {
    List<CurrencyDTO> getAllCurrencies();

    CurrencyDTO findByCode(String code);

    CurrencyDTO addNewCurrency(String code, String name, String sign);
}