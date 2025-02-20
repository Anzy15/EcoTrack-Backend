package com.capstone.EcoTrack.controller;

import com.capstone.EcoTrack.service.AuthService;
import com.capstone.EcoTrack.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            String token = authService.registerUser(registerRequest.getEmail(), registerRequest.getPassword());
            return ResponseEntity.ok("User registered successfully. Firebase Token: " + token);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Registration Failed: " + e.getMessage());
        }
    }

    //@PostMapping("/login")
   // public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    //    try {
        //    String token = authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
       //     return ResponseEntity.ok("Firebase Token: " + token);
      //  } catch (Exception e) {
       //     return ResponseEntity.status(401).body("Login Failed: " + e.getMessage());
      //  }
   // }
}