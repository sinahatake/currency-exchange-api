package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ExchangeRateDto;
import org.example.exceptions.InvalidParameterException;
import org.example.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRates")
public class ExchangeRateServlet extends BaseServlet {
    private ExchangeRateService exchangeRateService;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServlet.class);

    @Override
    public void init() {
        this.exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("GET request for all exchange rates");
        writeJson(response, HttpServletResponse.SC_OK, exchangeRateService.getAllExchangeRates());

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rateParam = request.getParameter("rate");

        logger.info("Attempting to create a new exchange rate: {} to {}", baseCurrencyCode, targetCurrencyCode);


        if (baseCurrencyCode == null || targetCurrencyCode == null || rateParam == null ||
            baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rateParam.isBlank()) {
            throw new InvalidParameterException("Missing or empty form parameters: baseCurrencyCode, targetCurrencyCode, and rate are required");
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateParam);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid rate format: rate must be a decimal number");
        }


        ExchangeRateDto newRate = exchangeRateService.addExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
        logger.info("Exchange rate created successfully with ID: {}", newRate.getId());

        writeJson(response, HttpServletResponse.SC_CREATED, newRate);

    }
}