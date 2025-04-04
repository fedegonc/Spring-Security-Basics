package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación consolidada del servicio de usuarios
 * que maneja tanto operaciones regulares como administrativas
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private ValidationAndNotificationService validationAndNotificationService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private RoleManagementService roleManagementService;

    private static final String IMAGE_UPLOAD_DIR = "./src/main/resources/static/img/";

    // Constructor para inyección de dependencias
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ======= Métodos básicos de acceso a datos =======

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    // Método privado para convertir una entidad User a un DTO UserDto
    private UserDto convertEntityToDto(User user) {
        UserDto userDto = new UserDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name.length > 0 ? name[0] : "");
        userDto.setLastName(name.length > 1 ? name[1] : "");
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
    @Override
    public User get(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Método para listar todos los roles
    @Override
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    // Método para guardar un usuario en la base de datos
    @Override
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

    // ======= Métodos para operaciones de usuarios regulares =======

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void eliminarEntidad(Long id) {
        userRepository.deleteById(id);
    }

    // Método para obtener el usuario actual sin requerir Optional
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

    @Override
    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return Optional.ofNullable(userRepository.findByUsername(userDetails.getUsername()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateUserProfile(User user, MultipartFile fileImage, String currentProfileImageUrl) throws IOException {
        if (fileImage != null && !fileImage.isEmpty()) {
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

    @Override
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        // Verificar que la contraseña actual es correcta
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        // Encriptar la nueva contraseña y actualizarla
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // ======= Métodos para operaciones administrativas =======
    
    @Override
    public boolean updateUserByAdmin(long id, User user, MultipartFile fileImage, 
                                   String currentProfileImage, String roleValue, 
                                   RedirectAttributes msg) {
        try {
            // Verificar si el usuario existe
            Optional<User> userExistente = userRepository.findById(id);
            if (!userExistente.isPresent()) {
                validationAndNotificationService.addErrorMessage(msg, "Usuario no encontrado");
                return false;
            }
            
            User existingUser = userExistente.get();
            
            // Validar unicidad de email si cambió
            if (!existingUser.getEmail().equals(user.getEmail()) && 
                !validationAndNotificationService.validateUniqueEmail(user.getEmail(), msg)) {
                return false;
            }
            
            // Validar unicidad de username si cambió
            if (!existingUser.getUsername().equals(user.getUsername()) && 
                !validationAndNotificationService.validateUniqueUsername(user.getUsername(), msg)) {
                return false;
            }
            
            // Actualizar los datos del usuario
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setUsername(user.getUsername());
            
            // Manejar la imagen de perfil si se proporciona una nueva
            if (fileImage != null && !fileImage.isEmpty()) {
                if (!fileStorageService.isValidImageFile(fileImage)) {
                    validationAndNotificationService.addErrorMessage(msg, "Por favor, sube solo imágenes (jpg, jpeg, png, gif)");
                    return false;
                }
                
                // Guardar la nueva imagen y eliminar la anterior si existe
                String newImageName = fileStorageService.storeImage(fileImage, currentProfileImage);
                existingUser.setProfileImage(newImageName);
            }
            
            // Guardar las actualizaciones del usuario
            userRepository.save(existingUser);
            
            // Asignar roles si se proporcionan
            if (roleValue != null && !roleValue.isEmpty()) {
                // Convertir la cadena de roles a una lista
                List<String> roleNames = Arrays.stream(roleValue.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
                
                // Usar RoleManagementService para asignar roles
                roleManagementService.setUserRoles(existingUser, roleNames, msg);
            }
            
            validationAndNotificationService.addSuccessMessage(msg, "Usuario actualizado correctamente");
            return true;
            
        } catch (Exception e) {
            validationAndNotificationService.addErrorMessage(msg, "Error al actualizar el usuario: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createUserByAdmin(User user, String password, String confirmPassword, 
                                    MultipartFile fileImage, String roleValue, 
                                    RedirectAttributes msg) throws IOException {
        try {
            // Validaciones básicas
            if (!password.equals(confirmPassword)) {
                validationAndNotificationService.addErrorMessage(msg, "Las contraseñas no coinciden");
                return false;
            }
            
            // Validar unicidad de email y username
            if (!validationAndNotificationService.validateUniqueEmail(user.getEmail(), msg)) {
                return false;
            }
            
            if (!validationAndNotificationService.validateUniqueUsername(user.getUsername(), msg)) {
                return false;
            }
            
            // Validar fortaleza de la contraseña
            if (!validationAndNotificationService.validatePasswordStrength(password, msg)) {
                return false;
            }
            
            // Encriptar la contraseña
            user.setPassword(passwordEncoder.encode(password));
            
            // Manejar la imagen de perfil
            if (fileImage != null && !fileImage.isEmpty()) {
                if (!fileStorageService.isValidImageFile(fileImage)) {
                    validationAndNotificationService.addErrorMessage(msg, "Por favor, sube solo imágenes (jpg, jpeg, png, gif)");
                    return false;
                }
                
                String imageName = fileStorageService.storeImage(fileImage, null);
                user.setProfileImage(imageName);
            }
            
            // Guardar el usuario
            User savedUser = userRepository.save(user);
            
            // Manejar roles si se proporcionan
            if (roleValue != null && !roleValue.isEmpty()) {
                List<String> roleNames = Arrays.stream(roleValue.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
                
                // Usar RoleManagementService para asignar roles
                roleManagementService.setUserRoles(savedUser, roleNames, msg);
            } else {
                // Asignar rol de administrador por defecto
                roleManagementService.assignRoleToUser(savedUser, "ROLE_ADMIN", msg);
            }
            
            validationAndNotificationService.addSuccessMessage(msg, "Usuario administrador creado exitosamente");
            return true;
            
        } catch (Exception e) {
            validationAndNotificationService.addErrorMessage(msg, "Error al crear el usuario: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteUserByAdmin(int id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si el usuario existe
            Optional<User> userOpt = userRepository.findById((long) id);
            if (!userOpt.isPresent()) {
                validationAndNotificationService.addErrorMessage(redirectAttributes, "Usuario no encontrado");
                return false;
            }
            
            User user = userOpt.get();
            
            // Eliminar la imagen de perfil asociada (si existe)
            String imageName = user.getProfileImage();
            if (imageName != null && !imageName.isEmpty()) {
                fileStorageService.deleteImage(imageName);
            }
            
            // Eliminar el usuario
            userRepository.deleteById((long) id);
            
            validationAndNotificationService.addSuccessMessage(redirectAttributes, "Usuario eliminado exitosamente");
            return true;
            
        } catch (Exception e) {
            validationAndNotificationService.addErrorMessage(redirectAttributes, 
                "Error al eliminar el usuario: " + e.getMessage());
            return false;
        }
    }
}
