package org.example.service;

import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.dto.ExchangeResultDTO;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private CurrencyService currencyService;
    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeService exchangeService;

    private CurrencyDTO usd;
    private CurrencyDTO eur;

    @BeforeEach
    void setUp() {
        usd = new CurrencyDTO(1, "USD", "US Dollar", "$");
        eur = new CurrencyDTO(2, "EUR", "Euro", "€");
    }

    @Test
    void exchange_DirectRate_ReturnsCorrectResult() {
        // Arrange
        BigDecimal rate = new BigDecimal("0.91");
        BigDecimal amount = new BigDecimal("100");

        when(currencyService.findByCode("USD")).thenReturn(usd);
        when(currencyService.findByCode("EUR")).thenReturn(eur);
        when(exchangeRateService.findByCodes("USD", "EUR"))
                .thenReturn(Optional.of(new ExchangeRateDTO(1, usd, eur, rate)));

        // Act
        ExchangeResultDTO result = exchangeService.exchange("USD", "EUR", amount);

        // Assert
        assertEquals(0, new BigDecimal("0.910000").compareTo(result.getRate()));
        assertEquals(0, new BigDecimal("91.00").compareTo(result.getConvertedAmount()));
        verify(exchangeRateService, times(1)).findByCodes("USD", "EUR");
    }

    @Test
    void exchange_ReverseRate_CalculatesCorrectly() {
        // Arrange
        BigDecimal reverseRate = new BigDecimal("2.0"); // EUR/USD = 2.0
        BigDecimal amount = new BigDecimal("10");

        when(currencyService.findByCode("USD")).thenReturn(usd);
        when(currencyService.findByCode("EUR")).thenReturn(eur);
        when(exchangeRateService.findByCodes("USD", "EUR")).thenReturn(Optional.empty());
        when(exchangeRateService.findByCodes("EUR", "USD"))
                .thenReturn(Optional.of(new ExchangeRateDTO(1, eur, usd, reverseRate)));

        // Act
        ExchangeResultDTO result = exchangeService.exchange("USD", "EUR", amount);

        // Assert
        // 1 / 2.0 = 0.5
        assertEquals(0, new BigDecimal("0.500000").compareTo(result.getRate()));
        assertEquals(0, new BigDecimal("5.00").compareTo(result.getConvertedAmount()));
    }

    @Test
    void exchange_CrossRate_CalculatesThroughUsd() {
        // Arrange
        CurrencyDTO gbp = new CurrencyDTO(3, "GBP", "Pound", "£");
        BigDecimal usdToEur = new BigDecimal("0.9");
        BigDecimal usdToGbp = new BigDecimal("0.8");
        BigDecimal amount = new BigDecimal("100");

        when(currencyService.findByCode("EUR")).thenReturn(eur);
        when(currencyService.findByCode("GBP")).thenReturn(gbp);

        // Прямого и обратного нет
        when(exchangeRateService.findByCodes("EUR", "GBP")).thenReturn(Optional.empty());
        when(exchangeRateService.findByCodes("GBP", "EUR")).thenReturn(Optional.empty());

        // Кросс-курсы через USD
        when(exchangeRateService.findByCodes("USD", "EUR")).thenReturn(Optional.of(new ExchangeRateDTO(1, usd, eur, usdToEur)));
        when(exchangeRateService.findByCodes("USD", "GBP")).thenReturn(Optional.of(new ExchangeRateDTO(2, usd, gbp, usdToGbp)));

        // Act
        ExchangeResultDTO result = exchangeService.exchange("EUR", "GBP", amount);

        // Assert
        // Rate = 0.8 / 0.9 = 0.888889
        assertEquals(0, new BigDecimal("0.888889").compareTo(result.getRate()));
    }

    @Test
    void exchange_InvalidCodes_ThrowsException() {
        assertThrows(InvalidParameterException.class, () ->
                exchangeService.exchange("US", "EUROPE", BigDecimal.TEN)
        );
    }

    @Test
    void exchange_RateNotFound_ThrowsEntityNotFoundException() {
        when(currencyService.findByCode(anyString())).thenReturn(usd);
        when(exchangeRateService.findByCodes(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                exchangeService.exchange("AAA", "BBB", BigDecimal.TEN)
        );
    }
}