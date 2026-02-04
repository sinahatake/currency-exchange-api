package org.example.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.example.dao.CurrencyDaoImpl;
import org.example.dao.ExchangeRateDaoImpl;
import org.example.mapper.CurrencyMapper;
import org.example.mapper.ExchangeRateMapper;
import org.example.service.CurrencyServiceImpl;
import org.example.service.ExchangeRateServiceImpl;
import org.example.service.ExchangeServiceImpl;
import org.mapstruct.factory.Mappers;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        var currencyDao = CurrencyDaoImpl.getInstance();
        var exchangeRateDao = ExchangeRateDaoImpl.getInstance();

        var currencyMapper = Mappers.getMapper(CurrencyMapper.class);
        var exchangeRateMapper = Mappers.getMapper(ExchangeRateMapper.class);

        var currencyService = new CurrencyServiceImpl(currencyDao, currencyMapper);
        var exchangeRateService = new ExchangeRateServiceImpl(
                exchangeRateDao,
                currencyDao,
                exchangeRateMapper,
                currencyService
        );
        var exchangeService = new ExchangeServiceImpl(
                currencyService,
                exchangeRateService
        );

        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute("currencyService", currencyService);
        servletContext.setAttribute("exchangeRateService", exchangeRateService);
        servletContext.setAttribute("exchangeService", exchangeService);
    }

}