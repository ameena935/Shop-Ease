package com.shop.ecommerce.controller;

import com.shop.ecommerce.config.ApiException;
import com.shop.ecommerce.dto.Requests;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository users, BCryptPasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody Requests.Register req) {
        if (users.existsByEmail(req.email().toLowerCase())) throw ApiException.bad("Email already registered");
        User u = new User();
        u.setName(req.name());
        u.setEmail(req.email().toLowerCase());
        u.setPassword(encoder.encode(req.password()));
        return issueToken(u);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody Requests.Login req) {
        User u = users.findByEmail(req.email().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return issueToken(u);
    }

    @GetMapping("/me")
    public User me(@RequestAttribute("user") User user) { return user; }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestAttribute("user") User user) {
        user.setToken(null);
        users.save(user);
        return Map.of("message", "Logged out");
    }

    private Map<String, Object> issueToken(User u) {
        u.setToken(UUID.randomUUID().toString());
        users.save(u);
        return Map.of("token", u.getToken(), "user", u);
    }
}
