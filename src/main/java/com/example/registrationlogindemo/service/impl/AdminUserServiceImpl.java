package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para operaciones administrativas relacionadas con usuarios
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private RoleManagementService roleManagementService;
    
    @Autowired
    private ValidationService validationService;
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public boolean updateUser(long id, User user, MultipartFile fileImage, 
                             String currentProfileImage, String roleValue, 
                             RedirectAttributes msg) throws IOException {
        try {
            // Verificar si el usuario existe
            Optional<User> userExistente = userRepository.findById(id);
            if (!userExistente.isPresent()) {
                notificationService.addErrorMessage(msg, "Usuario no encontrado con ID: " + id);
                return false;
            }
            
            User userActual = userExistente.get();
            
            // Verificar si el email o username ya están en uso por otro usuario
            if (!userActual.getEmail().equals(user.getEmail()) && 
                !validationService.validateUniqueEmail(user.getEmail(), msg)) {
                return false;
            }
            
            if (!userActual.getUsername().equals(user.getUsername()) && 
                !validationService.validateUniqueUsername(user.getUsername(), msg)) {
                return false;
            }
            
            // Actualizar atributos básicos
            userActual.setName(user.getName());
            userActual.setEmail(user.getEmail());
            userActual.setUsername(user.getUsername());
            
            // Manejar la imagen de perfil (si se proporciona una nueva)
            if (fileImage != null && !fileImage.isEmpty()) {
                if (!fileStorageService.isValidImageFile(fileImage)) {
                    notificationService.addErrorMessage(msg, "Por favor, sube solo imágenes (jpg, jpeg, png, gif)");
                    return false;
                }
                
                String imageName = fileStorageService.storeImage(fileImage, currentProfileImage);
                userActual.setProfileImage(imageName);
            }
            
            // Manejar roles si se proporcionan
            if (roleValue != null && !roleValue.isEmpty()) {
                List<String> roleNames = Arrays.stream(roleValue.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
                
                // Usar RoleManagementService para asignar roles
                roleManagementService.setUserRoles(userActual, roleNames, msg);
            }
            
            // Guardar el usuario actualizado
            userRepository.save(userActual);
            
            notificationService.addSuccessMessage(msg, "Usuario actualizado exitosamente");
            return true;
            
        } catch (Exception e) {
            notificationService.addErrorMessage(msg, "Error al actualizar el usuario: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createAdminUser(User user, String password, String confirmPassword, 
                                  MultipartFile fileImage, String roleValue, 
                                  RedirectAttributes msg) throws IOException {
        try {
            // Validaciones básicas
            if (!password.equals(confirmPassword)) {
                notificationService.addErrorMessage(msg, "Las contraseñas no coinciden");
                return false;
            }
            
            // Validar unicidad de email y username
            if (!validationService.validateUniqueEmail(user.getEmail(), msg)) {
                return false;
            }
            
            if (!validationService.validateUniqueUsername(user.getUsername(), msg)) {
                return false;
            }
            
            // Validar fortaleza de la contraseña
            if (!validationService.validatePasswordStrength(password, msg)) {
                return false;
            }
            
            // Encriptar la contraseña
            user.setPassword(passwordEncoder.encode(password));
            
            // Manejar la imagen de perfil
            if (fileImage != null && !fileImage.isEmpty()) {
                if (!fileStorageService.isValidImageFile(fileImage)) {
                    notificationService.addErrorMessage(msg, "Por favor, sube solo imágenes (jpg, jpeg, png, gif)");
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
            
            notificationService.addSuccessMessage(msg, "Usuario administrador creado exitosamente");
            return true;
            
        } catch (Exception e) {
            notificationService.addErrorMessage(msg, "Error al crear el usuario: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteUser(int id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si el usuario existe
            Optional<User> userOpt = userRepository.findById((long) id);
            if (!userOpt.isPresent()) {
                notificationService.addErrorMessage(redirectAttributes, "Usuario no encontrado");
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
            
            notificationService.addSuccessMessage(redirectAttributes, "Usuario eliminado exitosamente");
            return true;
            
        } catch (Exception e) {
            notificationService.addErrorMessage(redirectAttributes, 
                "Error al eliminar el usuario: " + e.getMessage());
            return false;
        }
    }
}
