package org.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.config.ApplicationContext;
import org.example.dto.ExchangeRateDTO;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.EntityNotFoundException;
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
    public void init() throws ServletException {
        this.exchangeRateService = ApplicationContext.exchangeRateService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        logger.info("GET request for all exchange rates");
        try {
            writeJson(response, HttpServletResponse.SC_OK, exchangeRateService.getAllExchangeRates());
        } catch (Exception e) {
            logger.error("Failed to fetch exchange rates", e);
            writeError(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rateParam = request.getParameter("rate");

        logger.info("Attempting to create a new exchange rate: {} to {}", baseCurrencyCode, targetCurrencyCode);

        try {
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

            ExchangeRateDTO newRate = exchangeRateService.addExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
            logger.info("Exchange rate created successfully with ID: {}", newRate.getId());

            writeJson(response, HttpServletResponse.SC_CREATED, newRate);

        } catch (InvalidParameterException e) {
            logger.warn("Validation error: {}", e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (AlreadyExistsException e) {
            logger.warn("Exchange rate for pair {}-{} already exists", baseCurrencyCode, targetCurrencyCode);
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("One or both currencies not found: {}", e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Critical error during POST /exchangeRates", e);
            writeError(response, 500, "Internal server error occurred");
        }
    }
}