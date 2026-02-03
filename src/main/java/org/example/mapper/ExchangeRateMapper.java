package org.example.mapper;

import org.example.dto.ExchangeRateDTO;
import org.example.entity.ExchangeRate;

public final class ExchangeRateMapper {
    private final CurrencyMapper currencyMapper;

    public ExchangeRateMapper(CurrencyMapper currencyMapper) {
        this.currencyMapper = currencyMapper;
    }

    public ExchangeRateDTO toDto(ExchangeRate rate) {
        ExchangeRateDTO dto = new ExchangeRateDTO();
        dto.setId(rate.getId());
        dto.setRate(rate.getRate());
        dto.setBaseCurrency(currencyMapper.toDto((rate.getBaseCurrency())));
        dto.setTargetCurrency(currencyMapper.toDto(rate.getTargetCurrency()));
        return dto;
    }

    public ExchangeRate toEntity(ExchangeRateDTO dto) {
        ExchangeRate rate = new ExchangeRate();
        rate.setId(dto.getId());
        rate.setRate(dto.getRate());
        rate.setBaseCurrency(currencyMapper.toEntity(dto.getBaseCurrency()));
        rate.setTargetCurrency(currencyMapper.toEntity(dto.getTargetCurrency()));
        return rate;
    }
}
