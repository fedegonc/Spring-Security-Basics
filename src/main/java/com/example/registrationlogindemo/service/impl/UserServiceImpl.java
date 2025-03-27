package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor para inyección de dependencias
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método privado para convertir una entidad User a un DTO UserDto
    private UserDto convertEntityToDto(User user) {
        UserDto userDto = new UserDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name[0]);
        userDto.setLastName(name[1]);
        userDto.setEmail(user.getEmail());

        return userDto;
    }

    // Método para guardar un nuevo usuario en la base de datos
    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername()); // Asignar el username
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());

        // Encriptar la contraseña
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Verificar si hay usuarios registrados y asignar el rol correspondiente
        long userCount = userRepository.count();
        Role role;

        if (userCount == 0) {
            // Primer usuario creado será el administrador
            role = roleRepository.findByName("ROLE_ADMIN");
            if (role == null) {
                role = checkRoleExist("ROLE_ADMIN");
            }
        } else {
            // El resto de usuarios serán usuarios normales
            role = roleRepository.findByName("ROLE_USER");
            if (role == null) {
                role = checkRoleExist("ROLE_USER");
            }
        }

        // Asignar el rol al usuario (evitando problemas de conversión)
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);
        
        userRepository.save(user);
    }

    // Método para buscar un usuario por correo electrónico
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Método para buscar un usuario por nombre
    @Override
    public User findByName(String name) {
        return userRepository.findByName(name);
    }

    // Método para obtener todos los usuarios y convertirlos a DTOs
    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    // Método privado para verificar si un rol ya existe en la base de datos
    private Role checkRoleExist(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    // Método para obtener un usuario por su ID
    public User get(Long id) {
        return userRepository.findById(id).get();
    }

    // Método para listar todos los roles
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    // Método para guardar un usuario en la base de datos
    public void save(User user) {
        userRepository.save(user);
    }

    // Método para obtener el ID de un usuario por su ID
    @Override
    public Long getUserById(Long userId) {
        return userId;
    }

    // Método para buscar un usuario por nombre de usuario
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
    @Override
    @Transactional
    public void eliminarEntidad(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = authentication.getName();
            return userRepository.findByUsername(username);
        }
        return null;
    }
    @Override
    public boolean debeMostrarLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si el usuario está autenticado (no es null y no es 'anonymousUser')
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return false; // Si el usuario está autenticado, no mostrar el botón de login
        }

        return true; // Si el usuario no está autenticado, mostrar el botón de login
    }

    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = authentication.getName();
            return Optional.ofNullable(userRepository.findByUsername(username));
        }
        return Optional.empty();
    }

    private static final String IMAGE_UPLOAD_DIR = "./src/main/resources/static/img/";


    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }


    public void updateUserProfile(User user, MultipartFile fileImage, String currentProfileImageUrl) throws IOException, IOException {
        if (!fileImage.isEmpty()) {
            String originalFilename = fileImage.getOriginalFilename();
            if (originalFilename != null) {
                String modifiedFilename = originalFilename.replace(" ", "_");
                byte[] bytes = fileImage.getBytes();
                Path path = Paths.get(IMAGE_UPLOAD_DIR + modifiedFilename);
                Files.write(path, bytes);
                user.setProfileImage(modifiedFilename);
            }
        } else {
            // Mantener la imagen de perfil actual si no se proporciona una nueva
            user.setProfileImage(currentProfileImageUrl);
        }

        userRepository.save(user);
    }
}
