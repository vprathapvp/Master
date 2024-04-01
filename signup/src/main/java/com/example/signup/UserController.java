package com.example.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists, please choose another one.");
        } else {
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<User> getUserProfile(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // Return only email, first name, and last name for display in profile
            User userProfile = new User();
            userProfile.setEmail(user.getEmail());
            userProfile.setFirstName(user.getFirstName());
            userProfile.setLastName(user.getLastName());
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
