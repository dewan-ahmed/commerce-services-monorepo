package com.acme.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "platform.security.entra.tenant-id=11111111-1111-1111-1111-111111111111",
        "platform.security.entra.audience=orders-api",
        "platform.security.entra.required-scope=orders.read"
})
@AutoConfigureMockMvc
class OrdersApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsRequestsWithoutBearerAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void servesOrdersEndpointForAuthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/orders").with(
                        SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders.read"))
                                .jwt(jwt -> jwt.audience(List.of("orders-api")))))
                .andExpect(status().isOk())
                .andExpect(content().string("orders-service"));
    }

    @Test
    void rejectsAuthenticatedRequestWithoutRequiredScope() throws Exception {
        mockMvc.perform(get("/api/orders").with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void exposesHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
