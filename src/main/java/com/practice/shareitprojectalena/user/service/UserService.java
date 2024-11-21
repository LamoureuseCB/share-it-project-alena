package com.practice.shareitprojectalena.user.service;

import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.mapper.UserMapper;
import com.practice.shareitprojectalena.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User create(User user) {
        Optional<User> userEmail = userRepository.findByEmail(user.getEmail());
        if (userEmail.isPresent()) {
            throw new ConflictException("Пользователь с данной электронной почтой уже существует");
        }
        return userRepository.create(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь по данному ID не найден"));
    }

    public User update(User updatedUser, Long id) {
        Optional<User> userEmail = userRepository.findByEmail(updatedUser.getEmail());
        if (userEmail.isPresent()) {
            User user = userEmail.get();
            if (!user.getId().equals(id)) {
                throw new ConflictException("Пользователь с данной электронной почтой уже существует");
            }
        }
        User existingUser = findById(id);
        userMapper.merge(existingUser, updatedUser);
        return userRepository.update(existingUser, id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void delete(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }
}








