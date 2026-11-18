package com.ticketbooking.service;

import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserCredentialsRepository;
import com.ticketbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final AuthService authService;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.demo-user.email}")
    private String demoUserEmail;

    @Value("${app.demo-user.password}")
    private String demoUserPassword;

    public DataInitializer(UserRepository userRepository,
                           UserCredentialsRepository credentialsRepository,
                           AuthService authService) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.authService = authService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createAdminIfMissing();
        givePasswordToDemoUser();
    }

    private void createAdminIfMissing() {
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        User admin;
        if (existingAdmin.isPresent()) {
            admin = existingAdmin.get();
        } else {
            admin = new User();
            admin.setFullName(adminFullName);
            admin.setEmail(adminEmail);
            admin.setPhone(adminPhone);
            admin = userRepository.save(admin);
        }

        if (credentialsRepository.findById(admin.getUserId()).isEmpty()) {
            authService.saveCredentials(admin, adminPassword, AuthService.ROLE_ADMIN, true);
        }
    }

    private void givePasswordToDemoUser() {
        Optional<User> demoUser = userRepository.findByEmail(demoUserEmail);
        if (demoUser.isPresent() && credentialsRepository.findById(demoUser.get().getUserId()).isEmpty()) {
            authService.saveCredentials(demoUser.get(), demoUserPassword, AuthService.ROLE_CUSTOMER, true);
        }
    }
}
