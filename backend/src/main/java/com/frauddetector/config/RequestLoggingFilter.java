package com.frauddetector.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs every inbound HTTP request and its outcome.
 * Format: [METHOD /path] user=email status=200 time=45ms
 * WARN if: response time > 2000ms OR status >= 400
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final long SLOW_REQUEST_THRESHOLD_MS = 2000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;

            String method = request.getMethod();
            String uri    = request.getRequestURI();
            int    status = response.getStatus();
            String user   = resolveUser();

            String logMessage = String.format("[%s %s] user=%s status=%d time=%dms",
                    method, uri, user, status, elapsed);

            if (elapsed > SLOW_REQUEST_THRESHOLD_MS || status >= 400) {
                log.warn(logMessage);
            } else {
                log.info(logMessage);
            }
        }
    }

    private String resolveUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {
            // Never break the filter chain due to logging failures
        }
        return "anonymous";
    }

    /**
     * Skip logging for Spring actuator health-check endpoints to reduce noise.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/favicon");
    }
}
