package org.example.service;

import org.example.dto.CurrencyDTO;
import org.example.dto.ExchangeRateDTO;
import org.example.dto.ExchangeResultDTO;
import org.example.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private CurrencyService currencyService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeService exchangeService;

    private CurrencyDTO usdDto;
    private CurrencyDTO eurDto;

    @BeforeEach
    void setUp() {
        usdDto = new CurrencyDTO();
        usdDto.setCode("USD");
        eurDto = new CurrencyDTO();
        eurDto.setCode("EUR");
    }

    @Test
    @DisplayName("Прямой курс: USD -> EUR")
    void calculateRate_DirectRate_ReturnsRate() throws SQLException {
        // Arrange
        ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
        exchangeRateDTO.setRate(new BigDecimal("0.90"));

        when(exchangeRateService.findByCodes("USD", "EUR")).thenReturn(Optional.of(exchangeRateDTO));

        // Act
        BigDecimal result = exchangeService.calculateRate("USD", "EUR");

        // Assert
        assertEquals(new BigDecimal("0.90"), result);
    }

    @Test
    @DisplayName("Обратный курс: EUR -> USD (1 / USD-EUR)")
    void calculateRate_ReverseRate_ReturnsInvertedRate() throws SQLException {
        // Arrange
        ExchangeRateDTO directRate = new ExchangeRateDTO();
        directRate.setRate(new BigDecimal("0.50")); // USD/EUR = 0.5

        when(exchangeRateService.findByCodes("EUR", "USD")).thenReturn(Optional.empty());
        when(exchangeRateService.findByCodes("USD", "EUR")).thenReturn(Optional.of(directRate));

        // Act
        BigDecimal result = exchangeService.calculateRate("EUR", "USD");

        // Assert
        // 1 / 0.50 = 2.00
        assertEquals(new BigDecimal("2.00"), result);
    }

    @Test
    @DisplayName("Кросс-курс через USD: GBP -> EUR")
    void calculateRate_CrossRateViaUsd_ReturnsCalculatedRate() throws SQLException {
        // Arrange
        ExchangeRateDTO usdToGbp = new ExchangeRateDTO();
        usdToGbp.setRate(new BigDecimal("0.80")); // USD/GBP

        ExchangeRateDTO usdToEur = new ExchangeRateDTO();
        usdToEur.setRate(new BigDecimal("0.90")); // USD/EUR

        when(exchangeRateService.findByCodes("GBP", "EUR")).thenReturn(Optional.empty());
        when(exchangeRateService.findByCodes("EUR", "GBP")).thenReturn(Optional.empty());
        when(exchangeRateService.findByCodes("USD", "GBP")).thenReturn(Optional.of(usdToGbp));
        when(exchangeRateService.findByCodes("USD", "EUR")).thenReturn(Optional.of(usdToEur));

        // Act
        BigDecimal result = exchangeService.calculateRate("GBP", "EUR");

        // Assert
        // 0.90 / 0.80 = 1.125 -> 1.13 (RoundingMode.HALF_UP, scale 2)
        assertEquals(new BigDecimal("1.13"), result);
    }

    @Test
    @DisplayName("Полный процесс обмена: расчет итоговой суммы")
    void exchange_FullProcess_ReturnsCorrectResult() throws SQLException {
        // Arrange
        String base = "USD";
        String target = "EUR";
        BigDecimal amount = new BigDecimal("100");
        BigDecimal rate = new BigDecimal("0.92");

        when(currencyService.findByCode(base)).thenReturn(usdDto);
        when(currencyService.findByCode(target)).thenReturn(eurDto);

        // Мокаем прямой курс внутри calculateRate
        ExchangeRateDTO rateDto = new ExchangeRateDTO();
        rateDto.setRate(rate);
        when(exchangeRateService.findByCodes(base, target)).thenReturn(Optional.of(rateDto));

        // Act
        ExchangeResultDTO result = exchangeService.exchange(base, target, amount);

        // Assert
        assertAll(
                () -> assertEquals(usdDto, result.getBaseCurrency()),
                () -> assertEquals(eurDto, result.getTargetCurrency()),
                () -> assertEquals(amount, result.getAmount()),
                () -> assertEquals(rate, result.getRate()),
                () -> assertEquals(new BigDecimal("92.00"), result.getConvertedAmount())
        );
    }

    @Test
    @DisplayName("Ошибка: курс не найден ни одним способом")
    void calculateRate_NotFound_ThrowsException() throws SQLException {
        // Arrange
        when(exchangeRateService.findByCodes(anyString(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> exchangeService.calculateRate("AAA", "BBB"));
    }
}