package com.capstone.EcoTrack.controller;

import com.capstone.EcoTrack.model.User;
import com.capstone.EcoTrack.model.UserPreferences;
import com.capstone.EcoTrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth1")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String userId = userService.registerUser(
                request.get("username"),
                request.get("firstName"),
                request.get("lastName"),
                request.get("email"),
                request.get("password"),
                request.get("role")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        String password = request.get("password");

        String token = userService.loginUser(identifier, password);

        if (token.equals("User not found!") || token.equals("Invalid password!")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", token);
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        try {
            Map<String, Object> profile = userService.getUserProfile(userId);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            userService.updateProfileInfo(
                userId,
                request.get("firstName"),
                request.get("lastName"),
                request.get("location")
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/email/{userId}")
    public ResponseEntity<?> updateEmail(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            userService.updateEmail(userId, request.get("newEmail"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/password/{userId}")
    public ResponseEntity<?> updatePassword(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            userService.updatePassword(userId, request.get("oldPassword"), request.get("newPassword"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/preferences/{userId}")
    public ResponseEntity<?> updatePreferences(@PathVariable String userId, @RequestBody UserPreferences preferences) {
        try {
            userService.updatePreferences(userId, preferences);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
