package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());

        //encrypt the password once we integrate spring security
        //user.setPassword(userDto.getPassword());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
        Role roleUser = roleRepository.findByName("ROLE_USER");

        List<Role> roles = new ArrayList<>();

        /*if("ROLE_ADMIN".equals(userDto.getRole())){
            roles.add(getRoleIfExist(roleAdmin));
            roles.add(getRoleIfExist(roleUser));
        } else {
            roles.add(getRoleIfExist(roleAdmin));
        }*/
        roles.add(roleUser);
        user.setRoles(roles);
        userRepository.save(user);
    }
    private  Role getRoleIfExist(Role role) { return role != null ? role : new Role(); }
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

    private UserDto convertEntityToDto(User user){
        UserDto userDto = new UserDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name[0]);
        userDto.setLastName(name[1]);
        userDto.setEmail(user.getEmail());

        return userDto;
    }

    private Role checkRoleExist(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if(role==null){
            role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }

        return role;
    }
}
