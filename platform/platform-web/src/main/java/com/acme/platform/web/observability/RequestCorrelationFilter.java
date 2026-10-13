package com.acme.platform.web.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestCorrelationFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "request_id";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        String requestId = UUID.randomUUID().toString();
        long startNanos = System.nanoTime();

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        boolean completed = false;
        try {
            filterChain.doFilter(request, response);
            completed = true;
        } finally {
            long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
            try {
                LOGGER.info("http_request",
                        StructuredArguments.keyValue("method", request.getMethod()),
                        StructuredArguments.keyValue("status", completed ? response.getStatus() : 500),
                        StructuredArguments.keyValue("duration_ms", durationMillis));
            } finally {
                if (previousRequestId == null) {
                    MDC.remove(REQUEST_ID_MDC_KEY);
                } else {
                    MDC.put(REQUEST_ID_MDC_KEY, previousRequestId);
                }
            }
        }
    }
}
