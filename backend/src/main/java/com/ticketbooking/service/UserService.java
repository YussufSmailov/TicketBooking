package com.ticketbooking.service;

import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public User findOrCreate(String fullName, String email, String phone) {
        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Введите имя");
        }
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный формат email");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        return userRepository.save(user);
    }
}
