package org.example.config;

import org.example.dao.CurrencyDao;
import org.example.dao.CurrencyDaoImpl;
import org.example.dao.ExchangeRateDao;
import org.example.dao.ExchangeRateDaoImpl;
import org.example.mapper.CurrencyMapper;
import org.example.mapper.ExchangeRateMapper;
import org.example.service.CurrencyService;
import org.example.service.ExchangeRateService;
import org.example.service.ExchangeService;

public class ApplicationContext {

    private static final CurrencyDao CurrencyDao = CurrencyDaoImpl.getInstance();
    private static final ExchangeRateDao ExchangeRateDao = ExchangeRateDaoImpl.getInstance();

    private static final CurrencyMapper currencyMapper = new CurrencyMapper();
    private static final ExchangeRateMapper exchangeRateMapper =
            new ExchangeRateMapper(currencyMapper);

    private static final CurrencyService currencyService =
            new CurrencyService(CurrencyDao, currencyMapper);

    private static final ExchangeRateService exchangeRateService =
            new ExchangeRateService(
                    ExchangeRateDao,
                    CurrencyDao,
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
