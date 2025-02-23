package com.practice.shareitserver.repositoryTest;


import com.practice.shareitserver.user.UserRepository;
import com.practice.shareitserver.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        User user = User.builder()
                .name("Test User")
                .email("testuser@test.com")
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("testuser@test.com");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser@test.com", foundUser.get().getEmail());

        Optional<User> notFoundUser = userRepository.findByEmail("anyEmail@test.com");
        assertFalse(notFoundUser.isPresent());
    }
}

