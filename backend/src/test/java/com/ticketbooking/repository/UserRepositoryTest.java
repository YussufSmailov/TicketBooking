package com.ticketbooking.repository;

import com.ticketbooking.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void secondUserWithSameEmailIsRejectedByDatabase() {
        User copy = new User();
        copy.setFullName("Amina Copy");
        copy.setEmail("amina@gmail.com");
        copy.setPhone("+77010000003");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(copy));
    }

    @Test
    void findsUserByEmail() {
        assertThat(userRepository.findByEmail("dana@gmail.com"))
                .isPresent()
                .get()
                .extracting(User::getFullName)
                .isEqualTo("Dana Nurlanovna");
    }
}
