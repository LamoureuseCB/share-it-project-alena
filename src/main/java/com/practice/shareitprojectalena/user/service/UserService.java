package com.practice.shareitprojectalena.user.service;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User create(User user) {
        return userRepository.create(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User update(User user){
        return userRepository.update(user);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public void delete(Long id) {
        if(!userRepository.existsById(id)){
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
         userRepository.deleteById(id);
    }
}








