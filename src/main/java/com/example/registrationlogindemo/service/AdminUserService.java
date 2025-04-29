package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.User;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Servicio para gestionar las operaciones de usuarios desde el panel de administración
 */
public interface AdminUserService {

    /**
     * Prepara el ModelAndView para la vista de usuarios
     */
    ModelAndView prepareUsersView(String viewName, String currentPage);

    /**
     * Prepara el ModelAndView para la vista de edición de usuario
     */
    ModelAndView prepareUserEditView(long userId, String viewName);

    /**
     * Prepara el ModelAndView para la vista de perfil
     */
    ModelAndView prepareProfileView(String viewName);

    /**
     * Prepara el ModelAndView para la vista de creación de usuario
     */
    ModelAndView prepareNewUserView(String viewName);

    /**
     * Actualiza el perfil de un usuario
     */
    ModelAndView updateUserProfile(User user, BindingResult result, MultipartFile file, 
                                  String currentImg, RedirectAttributes msg, String redirectUrl);

    /**
     * Actualiza un usuario existente
     */
    String updateUser(User user, BindingResult result, long id, List<Long> roleIds, 
                     MultipartFile fileImage, RedirectAttributes msg);

    /**
     * Crea un nuevo usuario
     */
    String createUser(User user, BindingResult result, String password, String confirmPassword, 
                     List<Long> roleIds, MultipartFile fileImage, RedirectAttributes msg);

    /**
     * Crea un nuevo rol
     */
    String createRole(String roleName, String returnUrl, RedirectAttributes msg);

    /**
     * Elimina un usuario por su ID
     */
    String deleteUser(long id, RedirectAttributes redirectAttributes);
}
