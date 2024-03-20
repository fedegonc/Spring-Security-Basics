package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    private UserDto convertEntityToDto(User user) {
        UserDto userDto = new UserDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name[0]);
        userDto.setLastName(name[1]);
        userDto.setEmail(user.getEmail());

        return userDto;
    }
    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());

        // Encrypt the password
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Check if there are any users registered
        long userCount = userRepository.count();
        Role role;
        if (userCount == 0) {
            // If no users are registered, assign root role
            role = roleRepository.findByName("ROLE_ROOT");
            if (role == null) {
                role = checkRoleExist("ROLE_ROOT");
            }
        } else if (userCount == 1) {
            // If there's one user registered, assign admin role
            role = roleRepository.findByName("ROLE_ADMIN");
            if (role == null) {
                role = checkRoleExist("ROLE_ADMIN");
            }
        } else {
            // If there are more than one registered users, assign user role
            role = roleRepository.findByName("ROLE_USER");
            if (role == null) {
                role = checkRoleExist("ROLE_USER");
            }
        }

        user.setRoles(List.of(role));
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }



    private Role checkRoleExist(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    public User get(Long id) {
        return userRepository.findById(id).get();
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();

    }

    public void save(User user) {
        userRepository.save(user);
    }
}
