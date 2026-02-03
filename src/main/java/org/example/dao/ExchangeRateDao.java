package org.example.dao;

import org.example.entity.ExchangeRate;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateDao {
    ExchangeRate save(ExchangeRate rate);
    List<ExchangeRate> findAll();
    Optional<ExchangeRate> findByCurrencyCodes(String code1, String code2);
    boolean update(ExchangeRate rate);
}
