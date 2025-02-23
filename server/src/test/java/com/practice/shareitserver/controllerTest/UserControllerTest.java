package com.practice.shareitserver.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitserver.user.UserController;
import com.practice.shareitserver.user.UserMapper;
import com.practice.shareitserver.user.UserRepository;
import com.practice.shareitserver.user.UserService;
import com.practice.shareitserver.user.dto.UserCreateDto;
import com.practice.shareitserver.user.dto.UserResponseDto;
import com.practice.shareitserver.user.dto.UserUpdateDto;
import com.practice.shareitserver.user.entity.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;
    @MockBean
    private UserRepository userRepository;

    @Test
    @SneakyThrows
    void createUser_ReturnsCreated() {
        UserCreateDto userCreateDto = new UserCreateDto("тест", "тест@тест.com");
        User user = new User(1L, "тест", "тест@тест.com", new ArrayList<>());
        UserResponseDto userResponseDto = new UserResponseDto(1L, "тест", "тест@тест.com");

        when(userMapper.fromCreate(any(UserCreateDto.class))).thenReturn(user);
        when(userService.create(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("тест"))
                .andExpect(jsonPath("$.email").value("тест@тест.com"));
    }

    @Test
    @SneakyThrows
    void findById_Succsessfully() {
        Long userId = 1L;
        User user = new User(userId, "тест", "тест@тест.com", new ArrayList<>());
        UserResponseDto userResponseDto = new UserResponseDto(userId, "тест", "тест@тест.com");

        when(userService.findById(userId)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponseDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("тест"))
                .andExpect(jsonPath("$.email").value("тест@тест.com"));
    }


    @Test
    @SneakyThrows
    void findAll() {
        List<User> users = new ArrayList<>();
        User user1 = new User(1L, "тест1", "тест1@тест.com", new ArrayList<>());
        User user2 = new User(2L, "тест2", "тест2@тест.com", new ArrayList<>());
        users.add(user1);
        users.add(user2);

        List<UserResponseDto> userResponseDtos = new ArrayList<>();
        UserResponseDto dto1 = new UserResponseDto(1L, "тест1", "тест1@тест.com");
        UserResponseDto dto2 = new UserResponseDto(2L, "тест2", "тест2@тест.com");
        userResponseDtos.add(dto1);
        userResponseDtos.add(dto2);


        when(userService.findAll()).thenReturn(users);
        when(userMapper.toResponse(users)).thenReturn(userResponseDtos);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("тест1"))
                .andExpect(jsonPath("$[0].email").value("тест1@тест.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("тест2"))
                .andExpect(jsonPath("$[1].email").value("тест2@тест.com"));
    }

    @Test
    @SneakyThrows
    void findAll_EmptyList() {
        when(userService.findAll()).thenReturn(new ArrayList<>());
        when(userMapper.toResponse(new ArrayList<>())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

    }

    @Test
    @SneakyThrows
    void update() {
        Long userId = 1L;

        User existingUser = new User(userId, "тест", "тест@тест.com", new ArrayList<>());

        UserUpdateDto updatedUserData = new UserUpdateDto("обновленный тест", "тест@тест.ru");

        User updatedUserWithNewData =
                User.builder()
                        .id(userId)
                        .name(updatedUserData.getName())
                        .email(updatedUserData.getEmail())
                        .items(new ArrayList<>())
                        .build();

        UserResponseDto expectedResponse =
                new UserResponseDto(userId, updatedUserData.getName(), updatedUserData.getEmail());


        when(userRepository.findByEmail(updatedUserData.getEmail())).thenReturn(Optional.empty());

        when(userService.findById(userId)).thenReturn(existingUser);

        when(userRepository.save(any(User.class))).thenReturn(updatedUserWithNewData);

        when(userService.update(any(User.class), eq(userId))).thenReturn(updatedUserWithNewData);

        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        when(userMapper.fromUpdate(any(UserUpdateDto.class))).thenReturn(updatedUserWithNewData);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserWithNewData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("обновленный тест"))
                .andExpect(jsonPath("$.email").value("тест@тест.ru"));
    }

    @Test
    @SneakyThrows
    void deleteUser_Successfully() {
        Long userId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void deleteUser_UnSuccessfully_UserNotFound() {
        Long userId = 999L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }
}
