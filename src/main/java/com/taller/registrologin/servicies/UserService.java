package com.taller.registrologin.servicies;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.taller.registrologin.models.Users;
import com.taller.registrologin.models.UserRole;
import com.taller.registrologin.repositories.UsersRepository;

@Service 
public class UserService {

    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users register (Users user){
        if (usersRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email ya existente");
        }

        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return usersRepository.save(user);
    }

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }
}
