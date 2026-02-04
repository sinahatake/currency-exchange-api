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

@WebServlet("/currency/*")
public class SingleCurrencyServlet extends BaseServlet {
    private CurrencyService currencyService;
    private static final Logger logger = LoggerFactory.getLogger(SingleCurrencyServlet.class);

    @Override
    public void init() {
        this.currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        logger.info("GET request for single currency with path: {}", pathInfo);

        if (pathInfo == null || pathInfo.equals("/")) {
            logger.warn("Currency code is missing in the request path");
            throw new InvalidParameterException("Currency code is missing in the path");
        }

        String currencyCode = pathInfo.replace("/", "").toUpperCase();
        logger.debug("Extracted currency code: {}", currencyCode);

        CurrencyDto dto = currencyService.findByCode(currencyCode);
        logger.info("Currency found: {} ({})", dto.getName(), dto.getCode());

        writeJson(response, HttpServletResponse.SC_OK, dto);


    }
}