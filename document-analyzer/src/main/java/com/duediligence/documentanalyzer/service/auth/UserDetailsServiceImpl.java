package com.duediligence.documentanalyzer.service.auth;

import com.duediligence.documentanalyzer.model.auth.User;
import com.duediligence.documentanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for loading user details for authentication
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return user;
    }

    /**
     * Load user by email (alternative method for email-based login)
     */
    @Transactional
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        return user;
    }

    /**
     * Load user by wallet address
     */
    @Transactional
    public UserDetails loadUserByWalletAddress(String walletAddress) throws UsernameNotFoundException {
        User user = userRepository.findByWalletAddressAndIsActiveTrue(walletAddress)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with wallet address: " + walletAddress));

        return user;
    }
}