package org.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.config.ApplicationContext;
import org.example.dto.ExchangeRateDTO;
import org.example.exceptions.EntityNotFoundException;
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
        this.exchangeRateService = ApplicationContext.exchangeRateService();
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
        response.setContentType("application/json;charset=UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("GET request for exchange rate at /exchangeRate{}", pathInfo);

        try {
            String pair = validateAndGetPair(pathInfo);
            String baseCode = pair.substring(0, 3);
            String targetCode = pair.substring(3, 6);

            ExchangeRateDTO dto = exchangeRateService.getExchangeRateByCodes(baseCode, targetCode);
            writeJson(response, HttpServletResponse.SC_OK, dto);

        } catch (InvalidParameterException e) {
            logger.warn("Invalid parameters for GET exchange rate: {}", e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Exchange rate not found: {}", e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching exchange rate for path {}", pathInfo, e);
            writeError(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("PATCH request for exchange rate at /exchangeRate{}", pathInfo);

        try {
            String pair = validateAndGetPair(pathInfo);
            String baseCode = pair.substring(0, 3);
            String targetCode = pair.substring(3, 6);

            String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String rateParam = null;
            if (body.contains("rate=")) {
                rateParam = body.split("=")[1].trim();
            }

            if (rateParam == null || rateParam.isBlank()) {
                logger.warn("Parameter 'rate' is missing in the request body");
                throw new InvalidParameterException("Missing parameter in form body: rate");
            }

            BigDecimal rate;
            try {
                rate = new BigDecimal(rateParam);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid rate format: must be a decimal number");
            }

            logger.info("Updating rate for {}-{} to {}", baseCode, targetCode, rate);

            ExchangeRateDTO dto = exchangeRateService.updateExchangeRate(baseCode, targetCode, rate);

            logger.info("Rate successfully updated for {}", pair);
            writeJson(response, HttpServletResponse.SC_OK, dto);

        } catch (InvalidParameterException e) {
            logger.warn("PATCH validation failed: {}", e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Exchange rate for update not found in database: {}", e.getMessage());
            writeError(response, 404, "Exchange rate not found");
        } catch (Exception e) {
            logger.error("Critical error during PATCH /exchangeRate", e);
            writeError(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    private String validateAndGetPair(String pathInfo) {
        if (pathInfo == null || pathInfo.length() != 7) {
            throw new InvalidParameterException("Invalid currency pair in path. Correct format is /ABCXYZ");
        }
        return pathInfo.substring(1).toUpperCase();
    }
}