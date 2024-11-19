package com.practice.shareitprojectalena.user.repository;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public  class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());

    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));

    }

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user,Long id) {
        users.put(id, user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);

    }
    @Override
    public Optional<User> findByEmail(String email){
       return users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

}
