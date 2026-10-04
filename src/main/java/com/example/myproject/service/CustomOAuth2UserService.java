package com.example.myproject.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.myproject.entity.User;
import com.example.myproject.repository.UserRepository;
import com.example.myproject.security.CustomOAuth2User;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Tìm hoặc tạo user
        User user = processOAuth2User(email, name);
        
        // Trả về CustomOAuth2User thay vì OAuth2User gốc
        return new CustomOAuth2User(oAuth2User, user);
    }
    
    private User processOAuth2User(String email, String name) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            
            // Cập nhật thông tin nếu cần
            if (name != null && !name.equals(existingUser.getFullName())) {
                existingUser.setFullName(name);
                userRepository.save(existingUser);
            }
            
            System.out.println("OAuth2: Found existing user - " + email);
            return existingUser;
        } else {
            // Tạo user mới
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : "OAuth User");
            newUser.setPassword((passwordEncoder.encode("123456"))); // OAuth users don't need password
            newUser.setRole(User.Role.CUSTOMER);
            newUser.setStatus(User.UserStatus.ACTIVE);
            
            try {
                User savedUser = userRepository.save(newUser);
                System.out.println("OAuth2: Created new user - " + email);
                return savedUser;
            } catch (Exception e) {
                System.err.println("OAuth2: Error creating user - " + e.getMessage());
                throw new OAuth2AuthenticationException("Could not create user account: " + e.getMessage());
            }
        }
    }
}