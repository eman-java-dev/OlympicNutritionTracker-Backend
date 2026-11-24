package com.eman.tracker.olympicnutritiontracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
class RequestEventFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestEventFilter.class);
    private static final long LOG_INTERVAL_MILLIS = 30_000;
    private final AtomicLong lastThrottleWindow = new AtomicLong(-1);
    private final AtomicLong lastFailureWindow = new AtomicLong(-1);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        response.setHeader("X-Request-ID", requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            if (status == 429) {
                logOnce(lastThrottleWindow, "rate_limited", status, requestId);
            } else if (status >= 500) {
                logOnce(lastFailureWindow, "server_error", status, requestId);
            }
        }
    }

    private static void logOnce(AtomicLong lastWindow, String event, int status, String requestId) {
        long window = System.currentTimeMillis() / LOG_INTERVAL_MILLIS;
        long previous = lastWindow.get();
        if (window > previous && lastWindow.compareAndSet(previous, window)) {
            LOG.warn("event={} status={} request_id={}", event, status, requestId);
        }
    }
}
