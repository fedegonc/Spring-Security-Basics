package com.example.registrationlogindemo.service.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;

@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // Extraer información del usuario de Google
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        
        // Error first: verificar si el email existe
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        // Buscar si el usuario ya existe
        User existingUser = userRepository.findByEmail(email);
        User user;
        
        // Si el usuario existe, usarlo; si no, crear uno nuevo
        if (existingUser != null) {
            user = existingUser;
        } else {
            // Obtener el nombre del usuario
            String name = (String) attributes.get("name");
            
            // Si no hay nombre, usar el email como nombre
            if (name == null) {
                name = email;
            }
            
            user = registerNewUser(email, name);
        }
        
        // Devolver un OAuth2User con la información necesaria
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email");
    }
    
    private User registerNewUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        
        // Generar un nombre de usuario basado en el email
        String username = email.substring(0, email.indexOf('@'));
        user.setUsername(username);
        
        // Asignar rol de usuario
        Role role = roleRepository.findByName("ROLE_USER");
        user.setRoles(Collections.singletonList(role));
        
        // Guardar el usuario
        return userRepository.save(user);
    }
}
