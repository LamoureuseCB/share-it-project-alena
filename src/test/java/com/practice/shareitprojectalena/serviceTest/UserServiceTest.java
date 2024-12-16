package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setName("Test User");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@test.com");
        updatedUser.setName("Updated Test User");
    }

    @Test
    void testCreateUserSuccess() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        User createdUser = userService.create(user);
        assertEquals("Test User", createdUser.getName());
    }

    @Test
    void testCreateUserConflict() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        assertThrows(ConflictException.class, () -> userService.create(user));
    }

    @Test
    void testFindByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User foundUser = userService.findById(1L);
        assertEquals("Test User", foundUser.getName());
    }

    @Test
    void testFindByIdNotSuccess() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.findById(1L));
    }

    @Test
    void testUpdateUserSuccess() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@test.com");
        updatedUser.setName("Updated User");
        Mockito.when(userRepository.findByEmail(updatedUser.getEmail())).thenReturn(Optional.of(updatedUser));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.update(updatedUser, 1L);
        assertEquals("Updated User", result.getName());
    }

    @Test
    void testDeleteUserSuccess() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.delete(1L);
    }

    @Test
    public void testDeleteUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.delete(1L));
    }
}
