package org.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.config.ApplicationContext;
import org.example.dto.ExchangeResultDTO;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.example.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private ExchangeService exchangeService;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeServlet.class);

    @Override
    public void init() throws ServletException {
        this.exchangeService = ApplicationContext.exchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String baseCode = request.getParameter("from");
        String targetCode = request.getParameter("to");
        String amountParam = request.getParameter("amount");

        logger.info("Exchange request: from={}, to={}, amount={}", baseCode, targetCode, amountParam);

        try {
            if (baseCode == null || targetCode == null || amountParam == null ||
                baseCode.isBlank() || targetCode.isBlank() || amountParam.isBlank()) {
                throw new InvalidParameterException("Missing required query parameters: from, to, and amount are required");
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountParam);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid amount format: must be a decimal number");
            }

            ExchangeResultDTO dto = exchangeService.exchange(baseCode, targetCode, amount);

            logger.info("Exchange calculated: {} {} -> {} {} (Rate: {})",
                    amount, baseCode, dto.getConvertedAmount(), targetCode, dto.getRate());

            writeJson(response, HttpServletResponse.SC_OK, dto);

        } catch (InvalidParameterException e) {
            logger.warn("Exchange validation failed: {}", e.getMessage());
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Exchange rate not found for {} -> {}", baseCode, targetCode);
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Critical error during currency exchange calculation", e);
            writeError(response, 500, "Internal server error: " + e.getMessage());
        }
    }
}