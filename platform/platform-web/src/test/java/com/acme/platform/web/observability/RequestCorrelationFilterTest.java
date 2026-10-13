package com.acme.platform.web.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestCorrelationFilterTest {
    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestCorrelationFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>() {
        @Override
        protected void append(ILoggingEvent event) {
            event.prepareForDeferredProcessing();
            super.append(event);
        }
    };

    @BeforeEach
    void captureLogs() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void cleanUp() {
        logger.detachAppender(appender);
        appender.stop();
        MDC.clear();
    }

    @Test
    void emitsCorrelatedJsonWithoutSensitiveRequestData() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/catalog");
        request.addHeader("Authorization", "Bearer sensitive-token");
        request.addHeader("X-Request-Id", "untrusted-client-value");
        request.setQueryString("secret=private-value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> response.setStatus(200));

        String requestId = response.getHeader("X-Request-Id");
        assertNotNull(requestId);
        assertDoesNotThrow(() -> UUID.fromString(requestId));
        assertNull(MDC.get("request_id"));
        String encodedLog = encode(appender.list.get(0));
        JsonNode json = new ObjectMapper().readTree(encodedLog);
        assertEquals(requestId, json.path("request_id").asText());
        assertEquals("GET", json.path("method").asText());
        assertEquals(200, json.path("status").asInt());
        assertTrue(json.has("duration_ms"));
        assertFalse(encodedLog.contains("sensitive-token"));
        assertFalse(encodedLog.contains("private-value"));
        assertFalse(encodedLog.contains("untrusted-client-value"));
    }

    @Test
    void restoresMdcAndLogsServerErrorsWhenRequestThrows() throws Exception {
        MDC.put("request_id", "previous-request");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(ServletException.class, () -> filter.doFilter(
                new MockHttpServletRequest("GET", "/api/orders"),
                response,
                (req, res) -> {
                    throw new ServletException("request failed");
                }));

        assertEquals("previous-request", MDC.get("request_id"));
        JsonNode json = new ObjectMapper().readTree(encode(appender.list.get(0)));
        assertEquals(500, json.path("status").asInt());
        assertEquals(response.getHeader("X-Request-Id"), json.path("request_id").asText());
    }

    private String encode(ILoggingEvent event) {
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setContext(logger.getLoggerContext());
        encoder.start();
        try {
            return new String(encoder.encode(event), java.nio.charset.StandardCharsets.UTF_8);
        } finally {
            encoder.stop();
        }
    }
}
