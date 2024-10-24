package com.todolist.api.auth;

import com.todolist.api.security.JwtService;
import com.todolist.api.user.User;
import com.todolist.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationResponse register(RegistrationRequest request) {
        //todo handle error if email was already used
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email(request.getEmail()).
                password(request.getPassword())
                .build();
        return authenticate(authRequest);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //todo handle errors if email/password was not found
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var claims = new HashMap<String, Object>();
        var user = (User) auth.getPrincipal();
        claims.put("name", user.getName());
        var token = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder().token(token).build();
    }
}
