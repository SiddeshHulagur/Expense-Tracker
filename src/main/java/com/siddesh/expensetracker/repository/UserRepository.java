package com.siddesh.expensetracker.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.siddesh.expensetracker.entity.User;

public interface UserRepository extends MongoRepository<User, Long> {

    // Spring Data MongoDB will automatically derive the query for this method
    // based on the method name. It will look for a user by their email.
    Optional<User> findFirstByEmail(String email);

    boolean existsByEmail(String email);
}