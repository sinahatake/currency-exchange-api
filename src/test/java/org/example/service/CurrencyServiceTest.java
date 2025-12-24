package org.example.service;

import org.example.MapperDTO.CurrencyMapper;
import org.example.dao.CurrencyDAO;
import org.example.dto.CurrencyDTO;
import org.example.entity.Currency;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyDAO currencyDAO;

    @Mock
    private CurrencyMapper currencyMapper;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    @DisplayName("getAllCurrencies должен возвращать список DTO")
    void getAllCurrencies_ReturnsList() throws SQLException {
        // Given
        Currency currency = new Currency(1, "USD", "US Dollar", "$");
        CurrencyDTO dto = new CurrencyDTO(1, "US Dollar", "USD", "$");

        when(currencyDAO.findAll()).thenReturn(List.of(currency));
        when(currencyMapper.toDto(currency)).thenReturn(dto);

        // When
        List<CurrencyDTO> result = currencyService.getAllCurrencies();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USD", result.getFirst().getCode());
        verify(currencyDAO).findAll();
    }

    @Test
    @DisplayName("findByCode должен возвращать DTO, если валюта найдена")
    void findByCode_Found_ReturnsDto() throws SQLException {
        // Given
        String code = "EUR";
        Currency currency = new Currency(2, code, "Euro", "€");
        CurrencyDTO dto = new CurrencyDTO(2, "Euro", code, "€");

        when(currencyDAO.findByCode(code)).thenReturn(Optional.of(currency));
        when(currencyMapper.toDto(currency)).thenReturn(dto);

        // When
        CurrencyDTO result = currencyService.findByCode(code);

        // Then
        assertEquals(code, result.getCode());
    }

    @Test
    @DisplayName("findByCode должен выбрасывать EntityNotFoundException, если код не найден")
    void findByCode_NotFound_ThrowsException() throws SQLException {
        // Given
        String code = "NON";
        when(currencyDAO.findByCode(code)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> currencyService.findByCode(code));
    }

    @Test
    @DisplayName("addNewCurrency должен сохранять валюту при корректных данных")
    void addNewCurrency_ValidData_SavesCurrency() throws SQLException {
        // Given
        String code = "GBP";
        String name = "British Pound";
        String sign = "£";
        Currency savedCurrency = new Currency(3, code, name, sign);
        CurrencyDTO expectedDto = new CurrencyDTO(3, name, code, sign);

        when(currencyDAO.findByCode(code)).thenReturn(Optional.empty());
        when(currencyDAO.save(any(Currency.class))).thenReturn(savedCurrency);
        when(currencyMapper.toDto(savedCurrency)).thenReturn(expectedDto);

        // When
        CurrencyDTO result = currencyService.addNewCurrency(code, name, sign);

        // Then
        assertNotNull(result);
        assertEquals(code, result.getCode());
        verify(currencyDAO).save(any(Currency.class));
    }

    @Test
    @DisplayName("addNewCurrency должен выбрасывать InvalidParameterException при неверном коде")
    void addNewCurrency_InvalidCode_ThrowsException() {
        // Код не из 3 символов
        assertThrows(InvalidParameterException.class,
                () -> currencyService.addNewCurrency("US", "Dollar", "$"));

        // Код null
        assertThrows(InvalidParameterException.class,
                () -> currencyService.addNewCurrency(null, "Dollar", "$"));
    }

    @Test
    @DisplayName("addNewCurrency должен выбрасывать AlreadyExistsException, если код уже занят")
    void addNewCurrency_AlreadyExists_ThrowsException() throws SQLException {
        // Given
        String code = "USD";
        when(currencyDAO.findByCode(code)).thenReturn(Optional.of(new Currency()));

        // When & Then
        assertThrows(AlreadyExistsException.class,
                () -> currencyService.addNewCurrency(code, "Dollar", "$"));

        verify(currencyDAO, never()).save(any());
    }
}