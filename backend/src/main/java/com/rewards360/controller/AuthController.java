
package com.rewards360.controller;

import com.rewards360.dto.AuthResponse;
import com.rewards360.dto.LoginRequest;
import com.rewards360.dto.RegisterRequest;
import com.rewards360.model.CustomerProfile;
import com.rewards360.model.Role;
import com.rewards360.model.User;
import com.rewards360.repository.UserRepository;
import com.rewards360.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .password(passwordEncoder.encode(req.password()))
                .role(Role.valueOf(req.role().toUpperCase()))
                .build();
        CustomerProfile profile = CustomerProfile.builder()
                .loyaltyTier("Bronze")
                .pointsBalance(2800)
                .preferences(req.preferences())
                .communication(req.communication())
                .user(user)
                .build();
        user.setProfile(profile);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        String token = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        ));
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name()));
    }
}
