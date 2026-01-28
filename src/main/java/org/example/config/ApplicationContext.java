package org.example.config;

import org.example.mapperDto.CurrencyMapper;
import org.example.mapperDto.ExchangeRateMapper;
import org.example.dao.CurrencyDAO;
import org.example.dao.ExchangeRateDAO;
import org.example.service.CurrencyService;
import org.example.service.ExchangeRateService;
import org.example.service.ExchangeService;

public class ApplicationContext {

    private static final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private static final ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();

    private static final CurrencyMapper currencyMapper = new CurrencyMapper();
    private static final ExchangeRateMapper exchangeRateMapper =
            new ExchangeRateMapper(currencyMapper);

    private static final CurrencyService currencyService =
            new CurrencyService(currencyDAO, currencyMapper);

    private static final ExchangeRateService exchangeRateService =
            new ExchangeRateService(
                    exchangeRateDAO,
                    currencyDAO,
                    exchangeRateMapper,
                    currencyService
            );

    private static final ExchangeService exchangeService =
            new ExchangeService(
                    currencyService,
                    exchangeRateService
            );

    public static CurrencyService currencyService() {
        return currencyService;
    }

    public static ExchangeRateService exchangeRateService() {
        return exchangeRateService;
    }

    public static ExchangeService exchangeService() {
        return exchangeService;
    }
}
