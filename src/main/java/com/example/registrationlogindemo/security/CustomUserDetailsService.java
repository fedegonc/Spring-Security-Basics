package com.example.registrationlogindemo.security;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Servicio para cargar detalles de usuario personalizados
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // Repositorios necesarios
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Método para cargar detalles del usuario por su email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca el usuario por su email
        User user = userRepository.findByEmail(email);

        // Si el usuario existe, crea y devuelve un UserDetails con sus detalles
        if (user != null) {
            return new org.springframework.security.core.userdetails.User(user.getName(),
                    user.getPassword(),
                    mapRolesToAuthorities(user.getRoles()));
        } else {
            // Si el usuario no existe, lanza una excepción de UsernameNotFoundException
            throw new UsernameNotFoundException("Nombre de usuario inválido");
        }
    }

    // Método para mapear roles a authorities
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    // Método para registrar un usuario con un rol por defecto
    public void registerUserWithDefaultRole(String email, String password) {
        // Busca el rol por defecto "ROLE_USER"
        Role userRole = roleRepository.findByName("ROLE_USER");

        // Si el rol por defecto no existe, lo crea y lo guarda en la base de datos
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
        }

        // Crea un nuevo usuario con el email, la contraseña codificada y el rol por defecto
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles((List<Role>) Collections.singleton(userRole));

        // Guarda el usuario en la base de datos
        userRepository.save(user);
    }
}
