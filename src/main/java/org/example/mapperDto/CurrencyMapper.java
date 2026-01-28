package org.example.mapperDto;

import org.example.dto.CurrencyDTO;
import org.example.entity.Currency;

public final class CurrencyMapper {

    public CurrencyMapper() {
    }

    public CurrencyDTO toDto(Currency currency) {
        CurrencyDTO dto = new CurrencyDTO();
        dto.setId(currency.getId());
        dto.setCode(currency.getCode());
        dto.setName(currency.getFullName());
        dto.setSign(currency.getSign());
        return dto;
    }

    public Currency toEntity(CurrencyDTO dto) {
        Currency currency = new Currency();
        currency.setId(dto.getId());
        currency.setCode(dto.getCode());
        currency.setFullName(dto.getName());
        currency.setSign(dto.getSign());
        return currency;
    }
}
