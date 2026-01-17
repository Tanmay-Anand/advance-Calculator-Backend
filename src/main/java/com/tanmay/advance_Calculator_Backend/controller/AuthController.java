package com.tanmay.advance_Calculator_Backend.controller;

import com.tanmay.advance_Calculator_Backend.domain.User;
import com.tanmay.advance_Calculator_Backend.dto.LoginRequest;
import com.tanmay.advance_Calculator_Backend.dto.RegisterRequest;
import com.tanmay.advance_Calculator_Backend.repository.UserRepository;
import com.tanmay.advance_Calculator_Backend.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authManager;

    public AuthController(UserRepository userRepo, BCryptPasswordEncoder encoder,
                          JwtUtils jwtUtils, AuthenticationManager authManager) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtils= jwtUtils;
        this.authManager= authManager;
    }

    //REGISTER ENDPOINT
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok("User Registered");
    }

    //LOGIN ENDPOINT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = userRepo.findByUsername(req.getUsername()).get();
        String token = jwtUtils.generateToken(user.getUsername(), user.getId());
            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("expiresIn", jwtUtils.getClaims(token).getExpiration().getTime());
            body.put("username", user.getUsername());
            return ResponseEntity.ok(body);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
