package com.practice.shareitprojectalena.user.repository;


import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();

    Optional<User> findById(Long id);

    User create(User user);

    User update(User user);

    void deleteById(Long id);


}
