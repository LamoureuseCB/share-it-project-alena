package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Spy
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
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.findById(1L));
    }

    @Test
    void testUpdateUserSuccess() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@test.com");
        updatedUser.setName("Updated User");
        when(userRepository.findByEmail(updatedUser.getEmail())).thenReturn(Optional.of(updatedUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.update(updatedUser, 1L);
        assertEquals("Updated User", result.getName());
    }

    @Test
    void update_emailExistsThrowsxception() {
        Long userId = 1L;
        String existingEmail = "existing@ex.com";

        User updatedUser = new User();
        updatedUser.setEmail(existingEmail);

        User existingUserWithSameEmail = new User();
        existingUserWithSameEmail.setId(2L);
        existingUserWithSameEmail.setEmail(existingEmail);

        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(existingUserWithSameEmail));

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.update(updatedUser, userId));

        assertEquals("Пользователь с данной электронной почтой уже существует", exception.getMessage());
 }
    @Test
    void findAll_ReturnsListOfUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User1");
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User2");
        List<User> expectedUsers = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> actualUsers = userService.findAll();

        assertEquals(expectedUsers.size(), actualUsers.size());
        assertEquals(expectedUsers, actualUsers); }

    @Test
    void findAll_returnsEmptyListUsersNotExist() {
        List<User> expectedUsers = Collections.emptyList();

        when(userRepository.findAll()).thenReturn(expectedUsers);
        List<User> actualUsers = userService.findAll();

        assertTrue(actualUsers.isEmpty());
        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.delete(1L);
    }

    @Test
    public void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.delete(1L));
    }
}
