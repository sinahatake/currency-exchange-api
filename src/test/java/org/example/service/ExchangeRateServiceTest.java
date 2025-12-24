package org.example.service;

import org.example.MapperDTO.ExchangeRateMapper;
import org.example.dao.ExchangeRateDAO;
import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.entity.ExchangeRate;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateDAO exchangeRateDAO;

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Должен вернуть список всех курсов валют")
    void getAllExchangeRates_ReturnsList() throws SQLException {
        // Arrange
        ExchangeRate entity = new ExchangeRate();
        ExchangeRateDTO dto = new ExchangeRateDTO();
        when(exchangeRateDAO.findAll()).thenReturn(List.of(entity));
        when(exchangeRateMapper.toDto(entity)).thenReturn(dto);

        // Act
        List<ExchangeRateDTO> result = exchangeRateService.getAllExchangeRates();

        // Assert
        assertEquals(1, result.size());
        verify(exchangeRateDAO).findAll();
    }

    @Test
    @DisplayName("Должен выбросить исключение, если курс не найден по кодам")
    void getExchangeRateByCodes_NotFound_ThrowsException() throws SQLException {
        // Arrange
        when(exchangeRateDAO.findByCurrencyCodes("USD", "EUR")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> exchangeRateService.getExchangeRateByCodes("USD", "EUR"));
    }

    @Test
    @DisplayName("Должен успешно добавить новый курс валют")
    void addExchangeRate_Success() throws SQLException {
        // Arrange
        String base = "USD";
        String target = "EUR";
        BigDecimal rate = BigDecimal.valueOf(0.92);

        CurrencyDTO baseDto = new CurrencyDTO();
        CurrencyDTO targetDto = new CurrencyDTO();

        when(currencyService.findByCode(base)).thenReturn(baseDto);
        when(currencyService.findByCode(target)).thenReturn(targetDto);
        when(exchangeRateDAO.findByCurrencyCodes(base, target)).thenReturn(Optional.empty());

        ExchangeRate entity = new ExchangeRate();
        when(exchangeRateMapper.toEntity(any(ExchangeRateDTO.class))).thenReturn(entity);
        when(exchangeRateDAO.save(entity)).thenReturn(entity);
        when(exchangeRateMapper.toDto(entity)).thenReturn(new ExchangeRateDTO());

        // Act
        ExchangeRateDTO result = exchangeRateService.addExchangeRate(base, target, rate);

        // Assert
        assertNotNull(result);
        verify(exchangeRateDAO).save(any());
    }

    @Test
    @DisplayName("Должен выбросить исключение при добавлении дубликата курса")
    void addExchangeRate_AlreadyExists_ThrowsException() throws SQLException {
        // Arrange
        when(currencyService.findByCode("USD")).thenReturn(new CurrencyDTO());
        when(currencyService.findByCode("EUR")).thenReturn(new CurrencyDTO());
        when(exchangeRateDAO.findByCurrencyCodes("USD", "EUR")).thenReturn(Optional.of(new ExchangeRate()));

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> exchangeRateService.addExchangeRate("USD", "EUR", BigDecimal.ONE));
    }

    @Test
    @DisplayName("Должен выбросить InvalidParameterException при некорректном коде валюты")
    void addExchangeRate_InvalidCode_ThrowsException() throws SQLException {
        // Act & Assert (код "US" слишком короткий)
        assertThrows(InvalidParameterException.class,
                () -> exchangeRateService.addExchangeRate("US", "EUR", BigDecimal.ONE));
    }

    @Test
    @DisplayName("Должен успешно обновить существующий курс")
    void updateExchangeRate_Success() throws SQLException {
        // Arrange
        String base = "USD";
        String target = "EUR";
        BigDecimal newRate = new BigDecimal("0.95");

        when(currencyService.findByCode(base)).thenReturn(new CurrencyDTO());
        when(currencyService.findByCode(target)).thenReturn(new CurrencyDTO());
        when(exchangeRateMapper.toEntity(any())).thenReturn(new ExchangeRate());
        when(exchangeRateDAO.update(any())).thenReturn(true);

        // Act
        ExchangeRateDTO result = exchangeRateService.updateExchangeRate(base, target, newRate);

        // Assert
        assertEquals(newRate.setScale(2), result.getRate());
        verify(exchangeRateDAO).update(any());
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего курса")
    void updateExchangeRate_NotFound_ThrowsException() throws SQLException {
        // Arrange
        when(exchangeRateDAO.update(any())).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> exchangeRateService.updateExchangeRate("USD", "EUR", BigDecimal.ONE));
    }
}