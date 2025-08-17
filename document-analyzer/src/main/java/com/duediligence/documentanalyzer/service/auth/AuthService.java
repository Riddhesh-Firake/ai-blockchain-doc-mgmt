package com.duediligence.documentanalyzer.service.auth;

import com.duediligence.documentanalyzer.dto.auth.AuthResponse;
import com.duediligence.documentanalyzer.dto.auth.LoginRequest;
import com.duediligence.documentanalyzer.dto.auth.SignupRequest;
import com.duediligence.documentanalyzer.model.auth.User;
import com.duediligence.documentanalyzer.repository.UserRepository;
import com.duediligence.documentanalyzer.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling user authentication operations
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse registerUser(SignupRequest signupRequest) {
        logger.info("Attempting to register user with email: {}", signupRequest.getEmail());

        // Check if username already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        // Check if wallet address already exists
        if (userRepository.existsByWalletAddress(signupRequest.getWalletAddress())) {
            throw new IllegalArgumentException("Wallet address is already registered!");
        }

        // Create new user account
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()),
                signupRequest.getWalletAddress()
        );

        userRepository.save(user);

        // Generate JWT token for the new user
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

        logger.info("User registered successfully with email: {}", signupRequest.getEmail());

        return new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getWalletAddress(),
                (long) jwtUtils.getJwtExpirationMs()
        );
    }

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

        try {
            // Find user by email first
            Optional<User> userOptional = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                throw new BadCredentialsException("Invalid email or password");
            }

            User user = userOptional.get();

            // Authenticate using username (since that's what Spring Security expects)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            logger.info("User authenticated successfully with email: {}", loginRequest.getEmail());

            return new AuthResponse(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    user.getWalletAddress(),
                    (long) jwtUtils.getJwtExpirationMs()
            );

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Get user by wallet address (for blockchain operations)
     */
    public Optional<User> getUserByWalletAddress(String walletAddress) {
        return userRepository.findByWalletAddressAndIsActiveTrue(walletAddress);
    }

    /**
     * Get current authenticated user
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String)) {

            User userDetails = (User) authentication.getPrincipal();
            return userRepository.findById(userDetails.getId());
        }
        return Optional.empty();
    }

    /**
     * Get current user's wallet address
     */
    public Optional<String> getCurrentUserWalletAddress() {
        return getCurrentUser().map(User::getWalletAddress);
    }

    /**
     * Validate if current user owns the wallet address
     */
    public boolean validateWalletAddressForCurrentUser(String walletAddress) {
        Optional<String> currentUserWallet = getCurrentUserWalletAddress();
        return currentUserWallet.isPresent() && currentUserWallet.get().equals(walletAddress);
    }

    /**
     * Get user statistics
     */
    public long getActiveUsersCount() {
        return userRepository.countActiveUsers();
    }
}