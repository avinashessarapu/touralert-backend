package com.touralert.controller;

import jakarta.validation.Valid;
import com.touralert.model.User;
import com.touralert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. SIGN-UP ENDPOINT: Register a new account
    // URL: POST http://localhost:8080/api/auth/register
    


@PostMapping("/register")
    public String registerUser(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "Error: Username is already taken!";
        }
        userRepository.save(user);
        return "User registered successfully! You can now log in.";
    }


    // 2. LOG-IN ENDPOINT: Verify credentials
    // URL: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public String loginUser(@RequestBody User loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        
        // Check if user exists and password matches perfectly
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(loginRequest.getPassword())) {
            return "Login successful! Welcome " + userOpt.get().getUsername() + " (" + userOpt.get().getRole() + ")";
        }
        
        return "Error: Invalid username or password.";
    }



    // 3. UPDATE USER PROFILE
    // URL: PUT http://localhost:8080/api/auth/update/1
    @PutMapping("/update/{id}")
    public String updateProfile(@PathVariable Long id, @Valid @RequestBody User updatedData) {
        return userRepository.findById(id).map(user -> {
            // Update fields with validated data
            user.setEmail(updatedData.getEmail());
            user.setPassword(updatedData.getPassword()); // In production, this would be hashed!
            
            userRepository.save(user);
            return "Profile for user '" + user.getUsername() + "' updated successfully!";
        }).orElseThrow(() -> new RuntimeException("Update failed: User profile not found with ID: " + id));
    }

}