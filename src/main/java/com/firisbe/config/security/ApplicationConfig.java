package com.firisbe.config.security;

import com.firisbe.model.Enum.Role;
import com.firisbe.model.Customer;
import com.firisbe.repository.jpa.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final CustomerRepository repository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findCustomerByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public String createAdminAccount() {
        if (repository.findCustomerByEmail("admin@admin.com").isEmpty()) {

            Customer admin = Customer.builder()
                    .name("admin")
                    .lastName("admin")
                    .email("admin@admin.com")
                    .password(passwordEncoder().encode("admin"))
                    .receivedTransfers(new ArrayList<>())
                    .sentTransfers(new ArrayList<>())
                    .role(Role.ROLE_ADMIN)
                    .build();
            repository.save(admin);
        }
        return "Ok";
    }
}

