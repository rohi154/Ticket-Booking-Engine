package com.example.samyad.auth;

import com.example.samyad.dto.RegisterRequest;
import com.example.samyad.user.User;
import com.example.samyad.user.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.samyad.dto.UserResponse;
import com.example.samyad.dto.LoginRequest;
import com.example.samyad.auth.JwtService;
import com.example.samyad.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {

    User user = userService.createUser(request);

    return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
    );
}

 
   @PostMapping("/login")
    public LoginResponse login( @Valid @RequestBody LoginRequest request) {

    User user = userService.login(
            request.getEmail(),
            request.getPassword()
    );

    String token = jwtService.generateToken(user.getEmail());

    return new LoginResponse(token);
}

}