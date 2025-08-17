// AuthResponse.java
package com.duediligence.documentanalyzer.dto.auth;

/**
 * DTO for authentication response
 */
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String walletAddress;
    private Long expiresIn;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String username, String email, String walletAddress, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.walletAddress = walletAddress;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}