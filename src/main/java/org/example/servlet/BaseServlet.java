package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ErrorResponseDto;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        ErrorResponseDto error = new ErrorResponseDto(message);
        writeJson(response, status, error);
    }

    protected void writeJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }
}