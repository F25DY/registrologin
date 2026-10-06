package com.taller.registrologin.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.registrologin.models.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    
}
