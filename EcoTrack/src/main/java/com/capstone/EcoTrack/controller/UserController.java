package com.capstone.EcoTrack.controller;


import com.capstone.EcoTrack.model.*;
import com.capstone.EcoTrack.service.*;
import com.google.firebase.auth.FirebaseAuthException;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔹 Register User
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            // ✅ Input validation
            if (!userData.containsKey("username") || !userData.containsKey("firstName") ||!userData.containsKey("lastName") ||
            	!userData.containsKey("email") ||!userData.containsKey("password") || !userData.containsKey("role")) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            String username = userData.get("username");
            String firstName = userData.get("firstName");
            String lastName = userData.get("lastName");
            String email = userData.get("email");
            String password = userData.get("password");
            String role = userData.get("role");

            // ✅ Call user service to register user
            String userId = userService.registerUser(username, firstName, lastName, email,  password, role);

            // ✅ Success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Firebase authentication failed: " + e.getMessage());

        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Firestore write error: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }


    // 🔹 Login User
    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
    try {
        if (loginData == null || !loginData.containsKey("identifier") || !loginData.containsKey("password")) {
            return ResponseEntity.badRequest().body("Username/Email and Password are required.");
        }

        String identifier = loginData.get("identifier");
        String password = loginData.get("password");

        // Find user by email or username
        User user = userService.getUserByEmailOrUsername(identifier);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid username/email or password.");
        }

        // Validate password
        boolean isPasswordValid = userService.validatePassword(user, password);
        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid username/email or password.");
        }

        // Generate authentication token
        String token = userService.generateToken(user);

        // Return success response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("token", token);
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body("An error occurred during login: " + e.getMessage());
    }
}




    // 🔹 Get User by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) throws ExecutionException, InterruptedException {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testGetEndpoint(){
    	return ResponseEntity.ok("Test Get Endpoint is WORKING!!!");
    }

    // 🔹 Update User
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        userService.updateUser(id, updates);
        return ResponseEntity.ok("User updated successfully");
    }

    // 🔹 Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) throws FirebaseAuthException {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Get User Profile
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        try {
            Map<String, Object> profile = userService.getUserProfile(userId);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(profile);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error fetching user profile: " + e.getMessage());
        }
    }

    // Update Profile Information
    @PutMapping("/profile/{userId}/info")
    public ResponseEntity<?> updateProfileInfo(
            @PathVariable String userId,
            @RequestBody Map<String, String> profileData) {
        try {
            if (!profileData.containsKey("firstName") || !profileData.containsKey("lastName") || !profileData.containsKey("location")) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            userService.updateProfileInfo(
                userId,
                profileData.get("firstName"),
                profileData.get("lastName"),
                profileData.get("location")
            );

            return ResponseEntity.ok("Profile information updated successfully");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error updating profile information: " + e.getMessage());
        }
    }

    // Update Email
    @PutMapping("/profile/{userId}/email")
    public ResponseEntity<?> updateEmail(
            @PathVariable String userId,
            @RequestBody Map<String, String> emailData) {
        try {
            if (!emailData.containsKey("newEmail")) {
                return ResponseEntity.badRequest().body("New email is required");
            }

            userService.updateEmail(userId, emailData.get("newEmail"));
            return ResponseEntity.ok("Email updated successfully");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED)
                    .body("Error updating email: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error updating email: " + e.getMessage());
        }
    }

    // Update Password
    @PutMapping("/profile/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> passwordData) {
        try {
            if (!passwordData.containsKey("newPassword")) {
                return ResponseEntity.badRequest().body("New password is required");
            }

            userService.updatePassword(userId, passwordData.get("newPassword"));
            return ResponseEntity.ok("Password updated successfully");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED)
                    .body("Error updating password: " + e.getMessage());
        }
    }

    // Update Preferences
    @PutMapping("/profile/{userId}/preferences")
    public ResponseEntity<?> updatePreferences(
            @PathVariable String userId,
            @RequestBody UserPreferences preferences) {
        try {
            userService.updatePreferences(userId, preferences);
            return ResponseEntity.ok("Preferences updated successfully");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error updating preferences: " + e.getMessage());
        }
    }
}
