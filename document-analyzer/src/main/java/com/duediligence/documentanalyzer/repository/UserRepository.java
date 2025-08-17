package com.duediligence.documentanalyzer.repository;

import com.duediligence.documentanalyzer.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by wallet address
     */
    Optional<User> findByWalletAddress(String walletAddress);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if wallet address exists
     */
    boolean existsByWalletAddress(String walletAddress);

    /**
     * Find active users by email
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Find active users by wallet address
     */
    Optional<User> findByWalletAddressAndIsActiveTrue(String walletAddress);

    /**
     * Update last login time for a user
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Deactivate user account
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") Long userId);

    /**
     * Count active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
}