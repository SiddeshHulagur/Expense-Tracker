package com.siddesh.expensetracker.service;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.siddesh.expensetracker.dto.AuthResponse;
import com.siddesh.expensetracker.dto.LoginRequest;
import com.siddesh.expensetracker.dto.RegisterRequest;
import com.siddesh.expensetracker.entity.User;
import com.siddesh.expensetracker.mongo.service.SequenceGeneratorService;
import com.siddesh.expensetracker.repository.UserRepository;

@Service
public class AuthService {

    private static final String USER_SEQUENCE = "user_sequence";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SequenceGeneratorService sequenceGeneratorService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       SequenceGeneratorService sequenceGeneratorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setId(sequenceGeneratorService.getNextSequence(USER_SEQUENCE));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                request.email(),
                user.getPassword(),
                Collections.emptyList()
        );

        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );

        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }
}