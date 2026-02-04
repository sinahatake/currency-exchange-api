package org.example.servlet;

import jakarta.servlet.ServletException;
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
import java.nio.charset.StandardCharsets;

@WebServlet("/exchangeRate/*")
public class SingleExchangeRateServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(SingleExchangeRateServlet.class);
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        this.exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        logger.info("Fetching exchange rate for path: {}", pathInfo);

        String pair = validateAndGetPair(pathInfo);
        String base = pair.substring(0, 3);
        String target = pair.substring(3, 6);

        ExchangeRateDto dto = exchangeRateService.getExchangeRateByCodes(base, target);

        logger.info("Exchange rate found: {}/{} = {}", base, target, dto.getRate());
        writeJson(response, HttpServletResponse.SC_OK, dto);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String pair = validateAndGetPair(pathInfo);

        logger.info("Attempting to update exchange rate for: {}", pair);

        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String rateParam = extractRate(body);

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateParam);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse rate from body: {}", rateParam);
            throw new InvalidParameterException("Invalid rate format");
        }

        String base = pair.substring(0, 3);
        String target = pair.substring(3, 6);

        ExchangeRateDto dto = exchangeRateService.updateExchangeRate(base, target, rate);

        logger.info("Successfully updated rate for {} to {}", pair, rate);
        writeJson(response, HttpServletResponse.SC_OK, dto);
    }

    private String validateAndGetPair(String pathInfo) {
        if (pathInfo == null || pathInfo.length() != 7) {
            logger.warn("Invalid path format provided: {}", pathInfo);
            throw new InvalidParameterException("Invalid currency pair in path. Correct format is /ABCXYZ");
        }
        return pathInfo.substring(1).toUpperCase();
    }

    private String extractRate(String body) {
        if (!body.contains("rate=")) {
            logger.warn("Request body does not contain 'rate' parameter");
            throw new InvalidParameterException("Missing parameter in form body: rate");
        }
        return body.split("=")[1].trim();
    }
}