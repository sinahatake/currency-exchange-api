package org.example.mapper;

import org.example.dto.ExchangeRateDto;
import org.example.entity.ExchangeRate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "reference", uses = {CurrencyMapper.class})
public interface ExchangeRateMapper {

    ExchangeRateDto toDto(ExchangeRate rate);

    ExchangeRate toEntity(ExchangeRateDto dto);
}