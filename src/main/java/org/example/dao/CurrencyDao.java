package org.example.dao;

import org.example.entity.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyDao {
    Currency save(Currency currency);
    List<Currency> findAll();
    Optional<Currency> findByCode(String code);
}