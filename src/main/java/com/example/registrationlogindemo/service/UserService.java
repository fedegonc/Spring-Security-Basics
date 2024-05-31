
package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;

import java.util.List;

public interface UserService {

    // Método para guardar un usuario a partir de un DTO de usuario
    void saveUser(UserDto userDto);

    // Método para encontrar un usuario por su correo electrónico
    User findByEmail(String email);

    // Método para encontrar un usuario por su nombre
    User findByName(String name);


    // Método para obtener una lista de todos los usuarios como DTOs de usuario
    List<UserDto> findAllUsers();

    // Método para obtener un usuario por su ID
    User get(Long id);

    // Método para obtener una lista de todos los roles
    List<Role> listRoles();

    // Método para guardar un usuario
    void save(User user);

    // Método para obtener el ID de un usuario por su ID
    Long getUserById(Long userId);

    User findByUsername(String username);


    void deleteUserById(Long id);

}
