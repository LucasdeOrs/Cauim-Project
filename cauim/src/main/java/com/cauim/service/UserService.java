package com.cauim.service;

import com.cauim.model.User;
import com.cauim.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado!");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username já cadastrado!");
        }
    
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha não pode estar vazia!");
        }
    
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole("USER");
        user.setStatus(true);
    
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!"));
    
        Optional.ofNullable(userDetails.getFullName()).ifPresent(existingUser::setFullName);
        Optional.ofNullable(userDetails.getUsername()).ifPresent(username -> {
            if (userRepository.findByUsername(username).isPresent() && !existingUser.getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username já cadastrado!");
            }
            existingUser.setUsername(username);
        });
        Optional.ofNullable(userDetails.getEmail()).ifPresent(email -> {
            if (userRepository.findByEmail(email).isPresent() && !existingUser.getEmail().equals(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado!");
            }
            existingUser.setEmail(email);
        });
        Optional.ofNullable(userDetails.getPasswordHash()).ifPresent(passwordHash -> {
            if (!passwordHash.isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(passwordHash));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha não pode estar vazia!");
            }
        });
        Optional.ofNullable(userDetails.getPhoneNumber()).ifPresent(existingUser::setPhoneNumber);
        Optional.ofNullable(userDetails.getBirthDate()).ifPresent(existingUser::setBirthDate);
        Optional.ofNullable(userDetails.getProfilePictureUrl()).ifPresent(existingUser::setProfilePictureUrl);
    
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }
        userRepository.deleteById(userId);
    }

    public void sendPasswordResetEmail(String email) {
       userRepository.findByEmail(email).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!"));
    }
}
