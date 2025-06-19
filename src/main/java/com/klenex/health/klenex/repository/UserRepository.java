package com.klenex.health.klenex.repository;

import com.klenex.health.klenex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
