package com.practice.shareitprojectalena.user.repository;


import com.practice.shareitprojectalena.user.entity.User;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long id);

    User create(User user);

    User update(User user, Long id);

    void deleteById(Long id);

    Optional<User> findByEmail(String email);

}
