package com.shopsphere.controller.api;

import com.shopsphere.dto.AuthRequest;
import com.shopsphere.dto.AuthResponse;
import com.shopsphere.entity.Customer;
import com.shopsphere.repository.CustomerRepository;
import com.shopsphere.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomerRepository customerRepository;

    public AuthApiController(AuthenticationManager authenticationManager,
                             JwtTokenProvider tokenProvider,
                             CustomerRepository customerRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        Customer customer = customerRepository.findByEmail(authRequest.getEmail()).orElseThrow();

        return ResponseEntity.ok(new AuthResponse(token, customer.getEmail(), customer.getName(), customer.getRole().name()));
    }
}
