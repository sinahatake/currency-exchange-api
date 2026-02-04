package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.CurrencyDTO;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.InvalidParameterException;
import org.example.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {
    private CurrencyService currencyService;
    private static final Logger logger = LoggerFactory.getLogger(CurrenciesServlet.class);

    @Override
    public void init() {
        this.currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        logger.info("GET request for all currencies");
        try {
            writeJson(response, HttpServletResponse.SC_OK, currencyService.getAllCurrencies());
        } catch (Exception e) {
            logger.error("Failed to fetch currencies", e);
            writeError(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        logger.info("Attempting to create a new currency: code={}, name={}", code, name);

        try {
            if (code == null || name == null || sign == null ||
                code.isBlank() || name.isBlank() || sign.isBlank()) {
                throw new InvalidParameterException("Missing or empty form parameters: name, code, and sign are required");
            }

            CurrencyDTO newCurrency = currencyService.addNewCurrency(code, name, sign);
            logger.info("Currency successfully saved: ID={}", newCurrency.getId());

            writeJson(response, HttpServletResponse.SC_CREATED, newCurrency);
            logger.debug("Response sent to client");

        } catch (InvalidParameterException e) {
            logger.warn("Validation error for currency {}: {}", code, e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (AlreadyExistsException e) {
            logger.warn("Currency with code {} already exists", code);
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Critical failure during POST /currencies for code {}", code, e);
            writeError(response, 500, "Internal server error occurred while processing your request");
        }
    }
}