package com.voting.config;

import com.voting.model.Role;
import com.voting.model.User;
import com.voting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(UserRepository repo, PasswordEncoder encoder,
                                  @Value("${app.admin.name}") String name,
                                  @Value("${app.admin.email}") String email,
                                  @Value("${app.admin.password}") String password) {
        return args -> {
            if (!repo.existsByEmail(email)) {
                repo.save(new User(name, email, encoder.encode(password), Role.ADMIN));
                System.out.println(">>> Default admin created: " + email);
            }
        };
    }
}
