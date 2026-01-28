package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ErrorResponseDTO;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponseDTO error = new ErrorResponseDTO(message);
        objectMapper.writeValue(response.getWriter(), error);
    }

    protected void writeJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }
}