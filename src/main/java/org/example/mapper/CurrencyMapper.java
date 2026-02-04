package org.example.mapper;

import org.example.dto.CurrencyDto;
import org.example.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "reference") // Используем простой доступ без DI контейнеров
public interface CurrencyMapper {

    @Mapping(source = "fullName", target = "name")
    CurrencyDto toDto(Currency currency);

    @Mapping(source = "name", target = "fullName")
    Currency toEntity(CurrencyDto dto);
}