package com.example.samyad.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String secret) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println(
                "JWT FILTER - "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );

        // No JWT supplied
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT FILTER - No Bearer token"
            );

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            // Validate JWT
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();

            System.out.println(
                    "JWT FILTER - Valid token for: "
                            + email
            );

            // Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.emptyList()
                    );

            // Create a fresh SecurityContext for this request
            SecurityContext context =
                    securityContextHolderStrategy.createEmptyContext();

            context.setAuthentication(authentication);

            // Store it using Spring Security's configured strategy
            securityContextHolderStrategy.setContext(context);

            System.out.println(
                    "JWT FILTER - Authentication set: "
                            + securityContextHolderStrategy
                                    .getContext()
                                    .getAuthentication()
                                    .getName()
            );

        } catch (Exception exception) {

            System.out.println(
                    "JWT FILTER - VALIDATION FAILED: "
                            + exception.getClass().getSimpleName()
                            + " - "
                            + exception.getMessage()
            );
        }

        // Continue through the Spring Security filter chain
        filterChain.doFilter(request, response);
    }
}