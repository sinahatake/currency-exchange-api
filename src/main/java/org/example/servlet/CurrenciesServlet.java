package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.CurrencyDto;
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
        logger.info("GET request for all currencies");
        writeJson(response, HttpServletResponse.SC_OK, currencyService.getAllCurrencies());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        logger.info("Attempting to create a new currency: code={}, name={}", code, name);

        if (code == null || name == null || sign == null ||
            code.isBlank() || name.isBlank() || sign.isBlank()) {
            throw new InvalidParameterException("Missing or empty form parameters: name, code, and sign are required");
        }

        CurrencyDto newCurrency = currencyService.addNewCurrency(code, name, sign);
        logger.info("Currency successfully saved: ID={}", newCurrency.getId());

        writeJson(response, HttpServletResponse.SC_CREATED, newCurrency);
        logger.debug("Response sent to client");

    }
}