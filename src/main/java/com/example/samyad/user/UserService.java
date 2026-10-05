package com.example.samyad.user;
import com.example.samyad.auth.InvalidCredentialsException;
import com.example.samyad.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(RegisterRequest request) {

        System.out.println("Password received: " + request.getPassword());

        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        System.out.println("Hashed password: " + hashedPassword);

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashedPassword);

        return userRepository.save(user);
    }
   public User login(String email, String rawPassword) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new InvalidCredentialsException(
                            "Invalid email or password"
                    ));

    boolean passwordMatches =
            passwordEncoder.matches(
                    rawPassword,
                    user.getPasswordHash()
            );

    if (!passwordMatches) {
        throw new InvalidCredentialsException(
                "Invalid email or password"
        );
    }

    return user;
}
}