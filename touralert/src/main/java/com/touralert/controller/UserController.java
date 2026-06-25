package com.touralert.controller;

import com.touralert.config.JwtUtil;
import com.touralert.model.User;
import com.touralert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 1. SECURE USER REGISTRATION (With Password Hashing)
    // URL: POST http://localhost:8080/api/users/register
    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "Error: Username is already taken!";
        }
        
        // Hash password before committing to physical storage
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole("USER"); // Default tier mapping
        }
        
        userRepository.save(user);
        return "User registration successful for: " + user.getUsername();
    }

    // 2. USER LOGIN & JWT DISPATCH GATEWAY
    // URL: POST http://localhost:8080/api/users/login
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        // Verify user existence and cryptographic password match
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            // Generate standard JWT access token bearer string
            String token = jwtUtil.generateToken(username);
            return Map.of(
                "status", "SUCCESS",
                "token", token,
                "role", userOpt.get().getRole(),
                "username", username
            );
        } else {
            throw new RuntimeException("Authentication Failed: Invalid username or password credentials.");
        }
    }

    // 3. SECURE PROFILE UPDATE
    // URL: PUT http://localhost:8080/api/users/{id}/profile
    @PutMapping("/{id}/profile")
    public String updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {
        
        return userRepository.findById(id).map(user -> {
            if (updates.containsKey("username")) {
                user.setUsername(updates.get("username"));
            }
            if (updates.containsKey("email")) {
                String newEmail = updates.get("email");
                if (userRepository.existsByEmailAndIdNot(newEmail, id)) {
                    throw new RuntimeException("Error: Email is already in use by another account.");
                }
                user.setEmail(newEmail);
            }
            if (updates.containsKey("password") && !updates.get("password").isBlank()) {
                user.setPassword(passwordEncoder.encode(updates.get("password")));
            }
            
            userRepository.save(user);
            return "Profile updated successfully for user ID: " + id;
        }).orElseThrow(() -> new RuntimeException("Error: User profile not found."));
    }
}