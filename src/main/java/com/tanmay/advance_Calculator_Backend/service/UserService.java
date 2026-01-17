package com.tanmay.advance_Calculator_Backend.service;

import com.tanmay.advance_Calculator_Backend.domain.User;
import com.tanmay.advance_Calculator_Backend.exception.ResourceNotFoundException;
import com.tanmay.advance_Calculator_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("User", "anonymous");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

//    public Long getCurrentUserId() {
//        return getCurrentUserId().getId();
//    }
}
