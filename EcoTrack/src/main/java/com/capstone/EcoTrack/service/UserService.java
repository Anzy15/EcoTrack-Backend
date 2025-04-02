package com.capstone.EcoTrack.service;

import com.capstone.EcoTrack.model.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Autowired
    public UserService(Firestore firestore, FirebaseAuth firebaseAuth, PasswordEncoder passwordEncoder, AuthService authService) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }
    
   
    public String registerUser(String username, String firstName, String lastName, String email, String password, String role) throws Exception {
        if (firestore == null) {
            throw new IllegalStateException("Firestore instance is not available!");
        }

        System.out.println("Starting user registration process...");
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);

        try {
            // Create user in Firebase Authentication
            System.out.println("Creating user in Firebase Auth...");
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(username);

            UserRecord userRecord = firebaseAuth.createUser(request);
            String userId = userRecord.getUid();
            System.out.println("Firebase Auth user created successfully with ID: " + userId);

            // Store user in Firestore Database
            System.out.println("Preparing Firestore document...");
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", userId);
            userMap.put("username", username);
            userMap.put("firstName", firstName);
            userMap.put("lastName", lastName);
            userMap.put("email", email);
            String encryptedPassword = passwordEncoder.encode(password);
            userMap.put("password", encryptedPassword);
            userMap.put("role", role);
            userMap.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            
            // Initialize default preferences
            UserPreferences preferences = new UserPreferences();
            userMap.put("preferences", preferences);

            System.out.println("Writing to Firestore...");
            try {
                ApiFuture<WriteResult> writeResult = firestore.collection("users").document(userId).set(userMap);
                WriteResult result = writeResult.get(); // Wait for the write to complete
                System.out.println("Firestore write successful! Update time: " + result.getUpdateTime());
                return userId;
            } catch (Exception e) {
                System.err.println("❌ Firestore write failed: " + e.getMessage());
                e.printStackTrace();
                // Attempt to delete the Firebase Auth user since Firestore write failed
                try {
                    firebaseAuth.deleteUser(userId);
                    System.out.println("Cleaned up Firebase Auth user after Firestore failure");
                } catch (Exception cleanupError) {
                    System.err.println("Failed to clean up Firebase Auth user: " + cleanupError.getMessage());
                }
                throw new RuntimeException("Failed to write user data to Firestore: " + e.getMessage(), e);
            }
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Firebase Auth error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create user in Firebase Auth: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }
    public User getUserByEmailOrUsername(String identifier) {
        // Use the injected Firestore instance
        CollectionReference users = firestore.collection("users");

        try {
            // Try fetching user by email
            Query query = users.whereEqualTo("email", identifier).limit(1);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(User.class);
            }

            // If not found by email, search by username
            query = users.whereEqualTo("username", identifier).limit(1);
            future = query.get();
            documents = future.get().getDocuments();
            
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(User.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // User not found
    }


    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword()); // Replace with hashed password validation
    }

    public String loginUser(String identifier, String password) {
        User user = getUserByEmailOrUsername(identifier);

        if (user == null) {
            return "User not found!";
        }

        if (!authService.validatePassword(user, password)) {
            return "Invalid password!";
        }

        return authService.generateToken(user);
    }

    // 🔹 Authenticate User (Login)
    public User authenticateUser(String email) throws ExecutionException, InterruptedException {
        CollectionReference usersRef = firestore.collection("users");
        Query query = usersRef.whereEqualTo("email", email).limit(1);
        ApiFuture<QuerySnapshot> future = query.get();

        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            DocumentSnapshot document = snapshot.getDocuments().get(0);
            return document.toObject(User.class);
        }

        return null; // User not found
    }

    //  Get User by ID
    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(userId);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }
    

    //  Update User
    public void updateUser(String userId, Map<String, Object> updates) {
        firestore.collection("users").document(userId).update(updates);
    }

    //  Delete User
    public void deleteUser(String userId) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(userId);
        firestore.collection("users").document(userId).delete();
    }

    // Update User Profile Information
    public void updateProfileInfo(String userId, String firstName, String lastName, String location) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("location", location);
        firestore.collection("users").document(userId).update(updates).get();
    }

    // Update User Email
    public void updateEmail(String userId, String newEmail) throws FirebaseAuthException, ExecutionException, InterruptedException {
        // Update in Firebase Auth
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userId)
                .setEmail(newEmail);
        firebaseAuth.updateUser(request);

        // Update in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);
        firestore.collection("users").document(userId).update(updates).get();
    }

    // Update User Password
    public void updatePassword(String userId, String oldPassword, String newPassword) throws Exception {
        // First, get the user from Firestore
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Verify the old password
        if (!validatePassword(user, oldPassword)) {
            throw new RuntimeException("Invalid old password");
        }

        try {
            // Update password in Firebase Auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userId)
                    .setPassword(newPassword);
            firebaseAuth.updateUser(request);

            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);

            // Update password in Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("password", hashedPassword);
            firestore.collection("users").document(userId).update(updates).get();

            System.out.println("Password updated successfully for user: " + userId);
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Firebase Auth error while updating password: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update password in Firebase Auth: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Error while updating password: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
        }
    }

    // Update User Preferences
    public void updatePreferences(String userId, UserPreferences preferences) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("preferences", preferences);
        firestore.collection("users").document(userId).update(updates).get();
    }

    // Get User Profile
    public Map<String, Object> getUserProfile(String userId) throws ExecutionException, InterruptedException {
        User user = getUserById(userId);
        if (user == null) {
            return null;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("username", user.getUsername());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("email", user.getEmail());
        profile.put("location", user.getLocation());
        profile.put("preferences", user.getPreferences());
        profile.put("createdAt", user.getCreatedAt());

        return profile;
    }
}
