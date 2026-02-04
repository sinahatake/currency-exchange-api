package org.example.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ErrorResponseDto;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.DatabaseException;
import org.example.exceptions.EntityNotFoundException;
import org.example.exceptions.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebFilter("/*")
public class ExceptionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (InvalidParameterException e) {
            logAndWriteError(res, req, e.getStatusCode(), e.getMessage(), "warn", e);
        } catch (EntityNotFoundException e) {
            logAndWriteError(res, req, e.getStatusCode(), e.getMessage(), "warn", e);
        } catch (AlreadyExistsException e) {
            logAndWriteError(res, req, e.getStatusCode(), e.getMessage(), "warn", e);
        } catch (DatabaseException e) {
            logAndWriteError(res, req, 500, "Database error: " + e.getMessage(), "error", e);
        } catch (Exception e) {
            logAndWriteError(res, req, 500, "Internal server error: " + e.getMessage(), "error", e);
        }
    }

    private void logAndWriteError(HttpServletResponse res, HttpServletRequest req, int status,
                                  String message, String logLevel, Exception e) throws IOException {

        String logMessage = String.format("Error during %s %s: %s", req.getMethod(), req.getRequestURI(), message);

        if ("error".equals(logLevel)) {
            logger.error(logMessage, e);
        } else {
            logger.warn(logMessage);
        }

        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), new ErrorResponseDto(message));
    }
}