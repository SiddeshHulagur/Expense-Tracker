package com.siddesh.expensetracker.repository;

import com.siddesh.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA will automatically create a query for this method
    // based on the method name. It will look for a user by their email.
    Optional<User> findByEmail(String email);
}