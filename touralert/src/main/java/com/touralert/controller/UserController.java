package com.touralert.controller;

import com.touralert.config.JwtUtil;
import com.touralert.model.User;
import com.touralert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.touralert.model.CoinTransaction;
import com.touralert.repository.CoinTransactionRepository;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CoinTransactionRepository coinTransactionRepository;

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
            User user = userOpt.get();
            String token = jwtUtil.generateToken(username);
            return Map.of(
                "status", "SUCCESS",
                "token", token,
                "role", user.getRole(),
                "username", username,
                "id", String.valueOf(user.getId()),
                "email", user.getEmail() == null ? "" : user.getEmail(),
                "coins", String.valueOf(user.getTripCoins() == null ? 0 : user.getTripCoins())
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

    // Debit user's TripCoins (used for small payments)
    // URL: PUT /api/users/{id}/coins/debit?amount=10
    @PutMapping("/{id}/coins/debit")
    public Map<String, Object> debitCoins(@PathVariable Long id, @RequestParam Integer amount) {
        return userRepository.findById(id).map(user -> {
            Integer current = user.getTripCoins() == null ? 0 : user.getTripCoins();
            if (amount == null || amount <= 0) throw new RuntimeException("Invalid amount");
            if (current < amount) throw new RuntimeException("Insufficient TripCoins");
            user.setTripCoins(current - amount);
            userRepository.save(user);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "OK");
            resp.put("coins", user.getTripCoins());
            return resp;
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/{id}/coins")
    public Map<String, Object> getCoins(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            Map<String, Object> resp = new HashMap<>();
            resp.put("coins", user.getTripCoins() == null ? 0 : user.getTripCoins());
            return resp;
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/{id}/coins/transactions")
    public List<CoinTransaction> getCoinTransactions(@PathVariable Long id) {
        return coinTransactionRepository.findByUserIdOrderByCreatedAtDesc(id);
    }

    // Spend coins and record transaction
    @PutMapping("/{id}/coins/spend")
    public Map<String, Object> spendCoins(@PathVariable Long id, @RequestParam Integer amount, @RequestParam(required = false) String reason) {
        if (amount == null || amount <= 0) throw new RuntimeException("Invalid amount");
        return userRepository.findById(id).map(user -> {
            Integer current = user.getTripCoins() == null ? 0 : user.getTripCoins();
            if (current < amount) throw new RuntimeException("Insufficient TripCoins");
            user.setTripCoins(current - amount);
            userRepository.save(user);
            CoinTransaction tx = new CoinTransaction();
            tx.setUserId(id);
            tx.setAmount(-amount);
            tx.setReason(reason == null ? "spend" : reason);
            coinTransactionRepository.save(tx);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "OK");
            resp.put("coins", user.getTripCoins());
            return resp;
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}