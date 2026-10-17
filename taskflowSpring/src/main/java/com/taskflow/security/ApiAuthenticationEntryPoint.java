package com.taskflow.security;

import com.taskflow.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 401 : pas de jeton, jeton invalide ou expiré. Réponse au format ApiError. */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponse writer;

    public ApiAuthenticationEntryPoint(ErrorResponse writer) {
        this.writer = writer;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        writer.errorLog(request, response, HttpStatus.UNAUTHORIZED, "Authentification requise : jeton absent, invalide ou expiré");
    }
}
