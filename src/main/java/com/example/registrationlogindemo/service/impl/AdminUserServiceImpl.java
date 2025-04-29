package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.AdminUserService;
import com.example.registrationlogindemo.service.FileStorageService;
import com.example.registrationlogindemo.service.RoleManagementService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar las operaciones de usuarios desde el panel de administración
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleManagementService roleManagementService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ValidationAndNotificationService validationService;
    
    private static final String ERROR_VALIDACION = "Por favor, corrija los errores en el formulario";

    /**
     * Prepara el ModelAndView para la vista de usuarios
     */
    @Override
    public ModelAndView prepareUsersView(String viewName, String currentPage) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            List<User> users = userService.findAll();
            mv.addObject("users", users);
            mv.addObject("currentPage", currentPage);
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar usuarios: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Prepara el ModelAndView para la vista de edición de usuario
     */
    @Override
    public ModelAndView prepareUserEditView(long userId, String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            User user = userService.get(userId);
            if (user == null) {
                mv.setViewName("redirect:/admin/users");
                return mv;
            }
            
            List<Role> allRoles = userService.listRoles();
            
            mv.addObject("user", user);
            mv.addObject("allRoles", allRoles);
            mv.addObject("currentPage", "Editar Usuario");
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el usuario: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Prepara el ModelAndView para la vista de perfil
     */
    @Override
    public ModelAndView prepareProfileView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();
            User user = userService.findByEmail(currentUserName);
            
            if (user == null) {
                mv.setViewName("redirect:/login");
                return mv;
            }
            
            mv.addObject("user", user);
            mv.addObject("currentPage", "Mi Perfil");
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el perfil: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Prepara el ModelAndView para la vista de creación de usuario
     */
    @Override
    public ModelAndView prepareNewUserView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            List<Role> allRoles = userService.listRoles();
            mv.addObject("user", new User());
            mv.addObject("allRoles", allRoles);
            mv.addObject("currentPage", "Nuevo Usuario");
        } catch (Exception e) {
            mv.addObject("error", "Error al preparar el formulario: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Actualiza el perfil de un usuario
     */
    @Override
    public ModelAndView updateUserProfile(User user, BindingResult result, MultipartFile file, 
                                         String currentImg, RedirectAttributes msg, String redirectUrl) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return new ModelAndView("redirect:" + redirectUrl);
        }
        
        try {
            userService.updateUserProfile(user, file, currentImg);
            validationService.addSuccessMessage(msg, "Perfil actualizado exitosamente");
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al actualizar perfil: " + e.getMessage());
        }
        
        return new ModelAndView("redirect:" + redirectUrl);
    }

    /**
     * Actualiza un usuario existente
     */
    @Override
    public String updateUser(User user, BindingResult result, long id, List<Long> roleIds, 
                           MultipartFile fileImage, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/editar/" + id;
        }
        
        try {
            User existingUser = userService.get(id);
            if (existingUser == null) {
                validationService.addErrorMessage(msg, "Usuario no encontrado");
                return "redirect:/admin/users";
            }
            
            // Actualizar datos básicos
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            
            // Actualizar roles si se proporcionan
            if (roleIds != null && !roleIds.isEmpty()) {
                // Convertir IDs de roles a nombres de roles
                List<String> roleNames = new ArrayList<>();
                for (Long roleId : roleIds) {
                    Role role = roleManagementService.getAllRoles().stream()
                            .filter(r -> r.getId().equals(roleId))
                            .findFirst()
                            .orElse(null);
                    if (role != null) {
                        roleNames.add(role.getName());
                    }
                }
                
                // Asignar roles al usuario
                if (!roleNames.isEmpty()) {
                    roleManagementService.setUserRoles(existingUser, roleNames, msg);
                }
            }
            
            // Actualizar imagen si se proporciona
            if (fileImage != null && !fileImage.isEmpty()) {
                String currentImage = existingUser.getProfileImage();
                userService.updateUserProfile(existingUser, fileImage, currentImage);
            } else {
                userService.save(existingUser);
            }
            
            validationService.addSuccessMessage(msg, "Usuario actualizado exitosamente");
            
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/admin/editar/" + id;
        }
        
        return "redirect:/admin/users";
    }

    /**
     * Crea un nuevo usuario
     */
    @Override
    public String createUser(User user, BindingResult result, String password, String confirmPassword, 
                           List<Long> roleIds, MultipartFile fileImage, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/newuser";
        }
        
        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            validationService.addErrorMessage(msg, "Las contraseñas no coinciden");
            return "redirect:/admin/newuser";
        }
        
        try {
            // Verificar si el email ya está registrado
            User existingUser = userService.findByEmail(user.getEmail());
            if (existingUser != null) {
                validationService.addErrorMessage(msg, "El email ya está registrado");
                return "redirect:/admin/newuser";
            }
            
            // Crear el usuario con roles
            String roleValue = roleIds != null ? roleManagementService.convertRoleIdsToString(roleIds) : "";
            boolean created = userService.createUserByAdmin(user, password, confirmPassword, fileImage, roleValue, msg);
            
            if (!created) {
                return "redirect:/admin/newuser";
            }
            
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear usuario: " + e.getMessage());
            return "redirect:/admin/newuser";
        }
        
        return "redirect:/admin/users";
    }

    /**
     * Crea un nuevo rol
     */
    @Override
    public String createRole(String roleName, String returnUrl, RedirectAttributes msg) {
        try {
            if (roleName == null || roleName.trim().isEmpty()) {
                validationService.addErrorMessage(msg, "El nombre del rol no puede estar vacío");
                return "redirect:" + (returnUrl != null ? returnUrl : "/admin/users");
            }
            
            // Normalizar el nombre del rol
            String normalizedRoleName = roleName.toUpperCase().startsWith("ROLE_") 
                ? roleName.toUpperCase() 
                : "ROLE_" + roleName.toUpperCase();
            
            roleManagementService.createRole(normalizedRoleName, msg);
            validationService.addSuccessMessage(msg, "Rol creado exitosamente: " + normalizedRoleName);
            
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear rol: " + e.getMessage());
        }
        
        return "redirect:" + (returnUrl != null ? returnUrl : "/admin/users");
    }

    /**
     * Elimina un usuario por su ID
     */
    @Override
    public String deleteUser(long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = userService.deleteUserByAdmin((int) id, redirectAttributes);
            if (!deleted) {
                return "redirect:/admin/users";
            }
            
            validationService.addSuccessMessage(redirectAttributes, "Usuario eliminado exitosamente");
            
        } catch (Exception e) {
            validationService.addErrorMessage(redirectAttributes, 
                    "Error al eliminar usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}
