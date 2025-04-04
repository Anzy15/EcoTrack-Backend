package com.capstone.EcoTrack.service;

import com.capstone.EcoTrack.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }
}