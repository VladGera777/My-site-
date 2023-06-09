package com.lpi.admissionscommittee.service;

import com.lpi.admissionscommittee.dto.UserDto;
import com.lpi.admissionscommittee.dto.other.UserDtoForEditing;
import com.lpi.admissionscommittee.entity.Role;
import com.lpi.admissionscommittee.entity.User;
import com.lpi.admissionscommittee.exceptions.NoSuchUserException;
import com.lpi.admissionscommittee.exceptions.UserAlreadyExistException;
import com.lpi.admissionscommittee.repository.UserRepository;
import com.lpi.admissionscommittee.service.mapper.UserMapper;
import com.lpi.admissionscommittee.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException(Constants.ENTER_USERNAME_TO_LOG_IN);
        }
        User user;
        if (username.contains("@")) {
            user = this.userRepository.findByEmail(username)
                    .orElseThrow(() -> new NoSuchUserException(Constants.COULD_NOT_FIND_USER_WITH_USERNAME + username));
        } else {
            user = this.userRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchUserException(Constants.COULD_NOT_FIND_USER_WITH_USERNAME + username));
        }
        return user;
    }

    public Long registerUser(UserDto userDto) throws UserAlreadyExistException {
        if (usernameExists(userDto.getUsername())) {
            throw new UserAlreadyExistException("Username: "
                    + userDto.getUsername() + " is taken");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("User with email: "
                    + userDto.getEmail() + " already exists");
        }
        userDto.setRole(Role.ADMIN);

        String password = userDto.getPassword();
        User newuser = User.builder()
                .email(userDto.getEmail())
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(password))
                .role(userDto.getRole())
                .isEnabled(true)
                .build();
        return userRepository.save(newuser).getId();
    }

    @Transactional
    public boolean changeBlockedStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchUserException(Constants.COULD_NOT_FIND_USER_WITH_USERNAME + username));
        user.setBlocked(!user.isBlocked());
        userRepository.save(user);
        return user.isBlocked();
    }

    private boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchUserException(Constants.COULD_NOT_FIND_USER_WITH_USERNAME + username));
        return mapper.mapToDto(user);
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
           throw new NoSuchUserException(Constants.COULD_NOT_FIND_USER_WITH_ID + id);
        }
        return mapper.mapToDto(user);
    }

    @Transactional
    public User editUser(UserDtoForEditing userDto) throws UserAlreadyExistException {
        String password = userDto.getPassword();
        if (password != null && !password.isBlank()) {
            userDto.setPassword(passwordEncoder.encode(password));
        }
        String username = userDto.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchUserException(Constants.COULD_NOT_FIND_USER_WITH_USERNAME + username));

        if (!user.getEmail().equalsIgnoreCase(userDto.getEmail())
                && userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("User with email: "
                    + userDto.getEmail() + " already exists");
        }
        mapper.mapToEntity(userDto, user);
        return userRepository.save(user);
    }
}
