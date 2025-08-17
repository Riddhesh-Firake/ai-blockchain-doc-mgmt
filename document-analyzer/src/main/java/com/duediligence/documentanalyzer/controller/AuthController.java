package com.duediligence.documentanalyzer.controller;

import com.duediligence.documentanalyzer.dto.auth.AuthResponse;
import com.duediligence.documentanalyzer.dto.auth.LoginRequest;
import com.duediligence.documentanalyzer.dto.auth.MessageResponse;
import com.duediligence.documentanalyzer.dto.auth.SignupRequest;
import com.duediligence.documentanalyzer.service.auth.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthService authService;

    /**
     * POST /api/auth/signin
     * User login endpoint
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException e) {
            logger.warn("Login failed for email: {} - Invalid credentials", loginRequest.getEmail());
            throw e; // Let global exception handler deal with it

        } catch (Exception e) {
            logger.error("Login error for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    /**
     * POST /api/auth/signup
     * User registration endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Registration attempt for email: {}, username: {}, wallet: {}",
                signupRequest.getEmail(), signupRequest.getUsername(), signupRequest.getWalletAddress());

        try {
            AuthResponse authResponse = authService.registerUser(signupRequest);
            return ResponseEntity.ok(authResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            throw e; // Let global exception handler deal with it

        } catch (Exception e) {
            logger.error("Registration error for email: {} - {}", signupRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * GET /api/auth/me
     * Get current user information
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            return authService.getCurrentUser()
                    .map(user -> {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("username", user.getUsername());
                        userInfo.put("email", user.getEmail());
                        userInfo.put("walletAddress", user.getWalletAddress());
                        userInfo.put("id", user.getId());
                        userInfo.put("createdAt", user.getCreatedAt().toString());
                        userInfo.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : null);
                        return ResponseEntity.ok(userInfo);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error retrieving user information");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }


    /**
     * POST /api/auth/validate-wallet
     * Validate if wallet address belongs to current user
     */
    @PostMapping("/validate-wallet")
    public ResponseEntity<Object> validateWalletAddress(@RequestBody String walletAddress) {
        try {
            boolean isValid = authService.validateWalletAddressForCurrentUser(walletAddress);
            return ResponseEntity.ok().body(new Object() {
                public final boolean valid = isValid;
                public final String message = isValid ?
                        "Wallet address is valid for current user" :
                        "Wallet address does not belong to current user";
            });

        } catch (Exception e) {
            logger.error("Error validating wallet address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error validating wallet address"));
        }
    }

    /**
     * POST /api/auth/signout
     * User logout endpoint
     */
    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> logoutUser() {
        try {
            // Clear the security context
            SecurityContextHolder.clearContext();

            logger.info("User signed out successfully");
            return ResponseEntity.ok(new MessageResponse("User signed out successfully"));

        } catch (Exception e) {
            logger.error("Error during sign out: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error during sign out"));
        }
    }

    /**
     * GET /api/auth/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Authentication service is running"));
    }

    /**
     * GET /api/auth/stats
     * Get authentication statistics (for admin purposes)
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        try {
            long activeUsers = authService.getActiveUsersCount();
            return ResponseEntity.ok().body(new Object() {
                public final long totalActiveUsers = activeUsers;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            });

        } catch (Exception e) {
            logger.error("Error getting auth stats: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error retrieving statistics"));
        }
    }
}