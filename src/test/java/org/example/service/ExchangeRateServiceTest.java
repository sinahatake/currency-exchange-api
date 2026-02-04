package org.example.service;

import org.example.dao.ExchangeRateDao;
import org.example.dto.CurrencyDto;
import org.example.dto.ExchangeRateDto;
import org.example.entity.ExchangeRate;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.example.mapper.ExchangeRateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateDao exchangeRateDAO;

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Должен вернуть список всех курсов валют")
    void getAllExchangeRates_ReturnsList() {
        // Arrange
        ExchangeRate entity = new ExchangeRate();
        ExchangeRateDto dto = new ExchangeRateDto();
        when(exchangeRateDAO.findAll()).thenReturn(List.of(entity));
        when(exchangeRateMapper.toDto(entity)).thenReturn(dto);

        // Act
        List<ExchangeRateDto> result = exchangeRateService.getAllExchangeRates();

        // Assert
        assertEquals(1, result.size());
        verify(exchangeRateDAO).findAll();
    }

    @Test
    @DisplayName("Должен выбросить исключение, если курс не найден по кодам")
    void getExchangeRateByCodes_NotFound_ThrowsException() {
        // Arrange
        when(exchangeRateDAO.findByCurrencyCodes("USD", "EUR")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> exchangeRateService.getExchangeRateByCodes("USD", "EUR"));
    }


    @Test
    @DisplayName("Должен выбросить исключение при добавлении дубликата курса")
    void addExchangeRate_AlreadyExists_ThrowsException() {
        // Arrange
        when(currencyService.findByCode("USD")).thenReturn(new CurrencyDto());
        when(currencyService.findByCode("EUR")).thenReturn(new CurrencyDto());
        when(exchangeRateDAO.findByCurrencyCodes("USD", "EUR")).thenReturn(Optional.of(new ExchangeRate()));

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> exchangeRateService.addExchangeRate("USD", "EUR", BigDecimal.ONE));
    }

    @Test
    @DisplayName("Должен выбросить InvalidParameterException при некорректном коде валюты")
    void addExchangeRate_InvalidCode_ThrowsException() {
        // Act & Assert (код "US" слишком короткий)
        assertThrows(InvalidParameterException.class,
                () -> exchangeRateService.addExchangeRate("US", "EUR", BigDecimal.ONE));
    }

    @Test
    @DisplayName("Должен успешно обновить существующий курс")
    void updateExchangeRate_Success() {
        // Arrange
        String base = "USD";
        String target = "EUR";
        BigDecimal newRate = new BigDecimal("0.95");

        // Подготавливаем сущность, которую якобы нашли в базе
        ExchangeRate existingEntity = new ExchangeRate();
        existingEntity.setId(1);

        // Настраиваем моки под НОВУЮ логику сервиса
        when(exchangeRateDAO.findByCurrencyCodes(base, target)).thenReturn(Optional.of(existingEntity));
        when(exchangeRateDAO.update(existingEntity)).thenReturn(true);

        // Настраиваем маппер, чтобы он вернул DTO с новым курсом
        ExchangeRateDto expectedDto = new ExchangeRateDto();
        expectedDto.setRate(newRate.setScale(2, RoundingMode.HALF_UP));
        when(exchangeRateMapper.toDto(existingEntity)).thenReturn(expectedDto);

        // Act
        ExchangeRateDto result = exchangeRateService.updateExchangeRate(base, target, newRate);

        // Assert
        assertNotNull(result);
        assertEquals(newRate.setScale(2, RoundingMode.HALF_UP), result.getRate());
        verify(exchangeRateDAO).findByCurrencyCodes(base, target);
        verify(exchangeRateDAO).update(existingEntity);
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего курса")
    void updateExchangeRate_NotFound_ThrowsException() {
        // Arrange
        String base = "USD";
        String target = "EUR";

        // Теперь сервис падает здесь, если пара не найдена в базе
        when(exchangeRateDAO.findByCurrencyCodes(base, target)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> exchangeRateService.updateExchangeRate(base, target, BigDecimal.ONE));

        // Проверяем, что до самого апдейта дело даже не дошло
        verify(exchangeRateDAO).findByCurrencyCodes(base, target);
    }


}