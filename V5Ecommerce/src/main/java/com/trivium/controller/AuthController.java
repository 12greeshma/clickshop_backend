
package com.trivium.controller;

import com.trivium.dto.*;
import com.trivium.entity.Customer;
import com.trivium.repository.CustomerRepository;
import com.trivium.service.CustomUserDetailsService;
import com.trivium.service.EmailService;
import com.trivium.config.JwtUtil;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private EmailService emailService;
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        // Check if email already exists
        if (customerRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Email is already registered. Please use a different email.");
        }
        
     // Generate OTP
        String otp = String.valueOf(new Random().nextInt(999999));

        Customer c = new Customer();
        c.setName(request.getName());
        c.setEmail(request.getEmail());
        c.setMobileNumber(request.getMobileNumber());
        c.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
     
        c.setRole("ROLE_CUSTOMER");
        c.setStatus("PENDING");
        c.setOtp(otp);
        c.setOtpGeneratedTime(LocalDateTime.now());
        customerRepo.save(c);

        emailService.sendOtpEmail(c.getEmail(), otp);

        return ResponseEntity.ok("OTP sent to your email. Please verify to complete registration.");
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationRequest request) {
        Customer customer = customerRepo.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }

        // Optionally check for expiry
        if (customer.getOtpGeneratedTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            return ResponseEntity.badRequest().body("OTP expired.");
        }

        customer.setStatus("ACTIVE");
        customer.setOtp(null); // Clear OTP
        customer.setOtpGeneratedTime(null); // Clear time
        customerRepo.save(customer);

        return ResponseEntity.ok("Email verified successfully! Registration completed.");
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        System.out.println("Login Request Email: " + request.getEmail());

        // 1. Authenticate the user
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword())
        );

        // 2. Loading user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String token = jwtUtil.generateToken(userDetails.getUsername(), role);

        System.out.println("Generated Token: " + token);
        System.out.println("User Role: " + role);

        // 3. role based
        if ("ROLE_CUSTOMER".equals(role)) {
            // Fetching customer details
            Customer customer = customerRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

         // ❗️ Check if status is ACTIVE
            if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
                throw new RuntimeException("Your account is not active. Please verify your email or contact support.");
            }

            return new AuthResponse(token, role, customer.getName(), customer.getEmail());
        } else if ("ROLE_ADMIN".equals(role)) {
           
            return new AuthResponse(token, role, "Admin", request.getEmail());
        } else {
            throw new RuntimeException("Unsupported role");
        }
    }


}
