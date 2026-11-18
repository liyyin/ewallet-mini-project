package com.ewallet.user_service.service;
import com.ewallet.user_service.dto.CreateUserRequest;
import com.ewallet.user_service.dto.UserResponse;
import com.ewallet.user_service.entity.User;
import com.ewallet.user_service.exception.BusinessException;
import com.ewallet.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Phone already registered");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User " + id + " not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getStatus(), user.getCreatedAt());
    }
}
