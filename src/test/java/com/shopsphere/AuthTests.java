package com.shopsphere;

import com.shopsphere.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String generateTestToken(String email, String role) {
        User principal = new User(email, "password", List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        return jwtTokenProvider.generateToken(auth);
    }

    /**
     * Test 1: JWT Token Generation and Validation
     */
    @Test
    @DisplayName("Auth Test 1: JWT generation and parsing")
    public void testJwtTokenGenerationAndValidation() {
        String token = generateTestToken("admin@shopsphere.com", "ADMIN");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("admin@shopsphere.com", jwtTokenProvider.getUsernameFromJwt(token));
    }

    /**
     * Test 2: Admin Access to Protected Endpoints
     */
    @Test
    @DisplayName("Auth Test 2: Admin granted access to product management")
    public void testAdminRoleAccessToProtectedEndpoints() throws Exception {
        String adminToken = generateTestToken("admin@shopsphere.com", "ADMIN");

        String productJson = "{\"name\":\"Admin Test Item\",\"price\":999.0,\"sku\":\"SKU-ADMIN-TEST\"}";

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated());
    }

    /**
     * Test 3: Customer Denied Admin Endpoints (403 Forbidden)
     */
    @Test
    @DisplayName("Auth Test 3: Customer forbidden from admin endpoints")
    public void testCustomerDeniedAdminEndpoints() throws Exception {
        String customerToken = generateTestToken("customer@shopsphere.com", "CUSTOMER");

        String productJson = "{\"name\":\"Unauthorized Item\",\"price\":999.0,\"sku\":\"SKU-NOPE\"}";

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 4: Unauthenticated Access Denied (403 or 401)
     */
    @Test
    @DisplayName("Auth Test 4: Unauthenticated user rejected on secured mutation")
    public void testUnauthenticatedAccessDenied() throws Exception {
        String productJson = "{\"name\":\"Unauthenticated Item\",\"price\":100.0,\"sku\":\"SKU-NOAUTH\"}";

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 5: Role-based Warehouse Manager Access
     */
    @Test
    @DisplayName("Auth Test 5: Warehouse manager permitted on restock")
    public void testRoleBasedWarehouseManagerAccess() throws Exception {
        String managerToken = generateTestToken("manager@shopsphere.com", "WAREHOUSE_MANAGER");

        String restockJson = "{\"productId\":1,\"warehouseId\":1,\"quantity\":15}";

        mockMvc.perform(post("/api/inventory/restock")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restockJson))
                .andExpect(status().isOk());
    }

    /**
     * Test 6: Invalid JWT Token Rejected
     */
    @Test
    @DisplayName("Auth Test 6: Tampered JWT token rejected")
    public void testInvalidJwtTokenRejected() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalid.payload.here";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }
}
