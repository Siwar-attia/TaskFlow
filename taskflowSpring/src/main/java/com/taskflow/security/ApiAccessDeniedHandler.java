package com.taskflow.security;

import com.taskflow.error.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 403 : jeton valide mais rôle insuffisant. Réponse au format ApiError. */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponse writer;

    public ApiAccessDeniedHandler(ErrorResponse writer) {
        this.writer = writer;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        writer.errorLog(request, response, HttpStatus.FORBIDDEN, "Accès refusé : droits insuffisants");
    }
}
