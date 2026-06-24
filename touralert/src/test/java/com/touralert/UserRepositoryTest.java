package com.touralert;

import com.touralert.model.User;
import com.touralert.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindUser() {
        // Given
        User user = new User();
        user.setUsername("testuser_test");
        user.setPassword("password123");
        user.setEmail("testuser_test@gmail.com");
        user.setRole("USER");

        // When
        User savedUser = userRepository.save(user);

        // Then
        Optional<User> foundUser = userRepository.findByUsername("testuser_test");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser_test@gmail.com");
    }
}