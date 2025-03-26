package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdministradorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SolicitudeRepository solicitudeRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SolicitudeService solicitudeService;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");
        
        // Obtener datos para las gráficas
        List<User> users = userRepository.findAll();
        List<Solicitude> solicitudes = solicitudeRepository.findAll();
        List<Report> reportes = reportRepository.findAll();
        
        // Contar totales
        int totalUsers = users.size();
        int totalSolicitudes = solicitudes.size();
        int totalReportes = reportes.size();
        
        // Agregar datos al modelo
        mv.addObject("users", users);
        mv.addObject("totalUsers", totalUsers);
        mv.addObject("totalSolicitudes", totalSolicitudes);
        mv.addObject("totalReportes", totalReportes);
        
        return mv;
    }

    @GetMapping("/profile")
    public ModelAndView viewProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        
        if (currentUser != null) {
            return new ModelAndView("redirect:/admin/profile/" + currentUser.getId());
        } else {
            return new ModelAndView("redirect:/error");
        }
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/user/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("admin/profile");
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
            }
            mv.addObject("user", user);
        }

        return mv;
    }

    @PostMapping("/profile/{id}")
    public ModelAndView editUser(@ModelAttribute("user") @Valid User user,
                                 BindingResult result,
                                 @PathVariable("id") long id,
                                 @RequestParam("fileImage") MultipartFile fileImage,
                                 @RequestParam("currentProfileImageUrl") String currentProfileImageUrl,
                                 RedirectAttributes msg) {

        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/admin/profile/" + id);
            return mv;
        }

        Optional<User> userEditOpt = userRepository.findById(id);

        if (userEditOpt.isPresent()) {
            User userEdit = userEditOpt.get();
            userEdit.setUsername(user.getUsername());
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());

            try {
                // Si se proporciona una nueva imagen de perfil
                if (!fileImage.isEmpty()) {
                    String originalFilename = fileImage.getOriginalFilename();
                    if (originalFilename != null) {
                        String modifiedFilename = originalFilename.replace(" ", "_");

                        byte[] bytes = fileImage.getBytes();
                        Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                        Files.write(path, bytes);
                        userEdit.setProfileImage(modifiedFilename);
                    }
                } else {
                    // Mantener la imagen de perfil actual si no se proporciona una nueva
                    userEdit.setProfileImage(currentProfileImageUrl);
                }
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al procesar la imagen de perfil: " + e.getMessage());
                mv.setViewName("redirect:/admin/profile/" + id);
                return mv;
            }

            // Guardar el usuario editado
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
            mv.setViewName("redirect:/admin/profile/" + user.getId());
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }

    @GetMapping("/reports")
    public ModelAndView adminReports() {
        ModelAndView mv = new ModelAndView("admin/reports");
        
        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);
        }
        
        // Obtener la lista de reportes
        List<Report> reportes = reportRepository.findAll();
        mv.addObject("reportes", reportes);
        
        return mv;
    }

    @GetMapping("/editereport/{id}")
    public ModelAndView adminEditReport(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/editreport");
        Optional<Report> image = reportRepository.findById(id);
        if (image.isPresent()) {
            mv.addObject("image", image.get());
        } else {
            mv.setViewName("redirect:/admin/dashboard");
        }
        return mv;
    }

    @GetMapping("/solicitudes")
    public ModelAndView adminSolicitudes() {
        ModelAndView mv = new ModelAndView("admin/solicitudes");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitudes", solicitude);
        return mv;
    }

    // Método para mostrar el formulario de edición de solicitud
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView adminEditSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("admin/editsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(id);

        if (solicitudeOptional.isPresent()) {
            Solicitude solicitude = solicitudeOptional.get();
            mv.addObject("solicitude", solicitude);
        } else {
            mv.setViewName("redirect:/error");
        }
        return mv;
    }

    // Método para procesar la edición de una solicitud
    @PostMapping("/editsolicitude/{id}")
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            // Si hay errores de validación, redirige con un mensaje de error
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/admin/editsolicitude/" + solicitude.getId();
        }
        // Obtener la solicitud que se va a editar
        Solicitude changeSolicitude = solicitudeRepository.findById(solicitude.getId()).orElse(null);
        if (changeSolicitude != null) {
            // Actualizar los campos de la solicitud con los nuevos valores
            changeSolicitude.setCategoria(solicitude.getCategoria());
            changeSolicitude.setDescripcion(solicitude.getDescripcion());

            // Actualizar campos de ubicación
            changeSolicitude.setBarrio(solicitude.getBarrio());
            changeSolicitude.setCalle(solicitude.getCalle());
            changeSolicitude.setNumeroDeCasa(solicitude.getNumeroDeCasa());

            try {
                // Guardar la imagen si se proporciona una
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get(UPLOAD_DIR + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            }
            // Guardar la solicitud editada en la base de datos
            solicitudeRepository.save(changeSolicitude);
            // Redirigir con un mensaje de éxito
            msg.addFlashAttribute("success", "Solicitud editada exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
        }

        return "redirect:/admin/solicitudes";
    }

    // Método para eliminar una solicitud
    @GetMapping("/deletsolicitude/{id}")
    public String adminExcluirSolicitud(@PathVariable("id") int id) {
        solicitudeRepository.deleteSolicitudeById((long) id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users")
    public ModelAndView rootUsers() {
        ModelAndView mv = new ModelAndView("admin/users");
        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

        }
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        return mv;
    }

    // Método para editar un usuario
    @GetMapping("/edit/{id}")
    public ModelAndView adminEditUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("admin/edit");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Obtener la lista de roles
            List<Role> listRoles = userService.listRoles();
            // Agregar el usuario y la lista de roles al modelo
            mv.addObject("user", user);
            mv.addObject("listRoles", listRoles);
        }
        return mv;
    }

    // Método para procesar la edición de un usuario
    @PostMapping("/edit/{id}")
    public String adminEditUserBanco(@PathVariable("id") long id,
                                   @RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "email", required = false) String email,
                                   @RequestParam(value = "roles", required = false) String roleValue,
                                   RedirectAttributes msg) {

        User userEdit = userRepository.findById(id).orElse(null);
        
        if (userEdit != null) {
            // Actualizar los datos del usuario con los nuevos valores
            if (name != null) userEdit.setName(name);
            if (email != null) userEdit.setEmail(email);
            
            // Gestionar los roles
            if (roleValue != null && !roleValue.isEmpty()) {
                // Limpiar roles existentes
                userEdit.getRoles().clear();
                
                // Buscar el rol, primero por nombre exacto
                Role role = roleRepository.findByName(roleValue);
                
                // Si no se encuentra por nombre y es ROLE_ORGANIZATION, crear el rol
                if (role == null && "ROLE_ORGANIZATION".equals(roleValue)) {
                    role = new Role();
                    role.setName("ROLE_ORGANIZATION");
                    role = roleRepository.save(role); // Guardar para obtener el ID
                }
                // Si no, intentar por ID
                else if (role == null) {
                    try {
                        Long roleId = Long.parseLong(roleValue);
                        role = roleRepository.findById(roleId).orElse(null);
                    } catch (NumberFormatException e) {
                        // No es un ID, dejamos role como null
                    }
                }
                
                // Si se encontró el rol, añadirlo al usuario
                if (role != null) {
                    userEdit.getRoles().add(role);
                } else {
                    msg.addFlashAttribute("error", "Rol no encontrado: " + roleValue + ". Verifique que exista en la base de datos.");
                }
            }

            // Guardar el usuario actualizado
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
            return "redirect:/admin/users";
        }
        
        msg.addFlashAttribute("error", "Usuario no encontrado.");
        return "redirect:/admin/users";
    }

    // Método para eliminar una solicitud
    @GetMapping("/deletuser/{id}")
    public String adminExcluirUser(@PathVariable("id") int id) {
        userService.deleteUserById((long) id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/crear-usuario")
    public ModelAndView mostrarFormularioCrearUsuario() {
        ModelAndView mv = new ModelAndView("admin/crear-usuario");
        // Crear un usuario vacío para el formulario
        User nuevoUsuario = new User();
        mv.addObject("user", nuevoUsuario);
        // Obtener la lista de roles disponibles
        List<Role> listRoles = userService.listRoles();
        mv.addObject("listRoles", listRoles);
        return mv;
    }

    @PostMapping("/crear-usuario")
    public String crearUsuario(@ModelAttribute("user") User user,
                              @RequestParam("password") String password,
                              @RequestParam(value = "roles", required = false) String roleValue,
                              RedirectAttributes redirectAttributes) {
        
        // Verificar si el usuario ya existe
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso");
            return "redirect:/admin/crear-usuario";
        }
        
        // Verificar si el email ya está en uso
        User existingEmail = userRepository.findByEmail(user.getEmail());
        if (existingEmail != null) {
            redirectAttributes.addFlashAttribute("error", "El email ya está en uso");
            return "redirect:/admin/crear-usuario";
        }
        
        // Crear un nuevo UserDto para usar el servicio existente
        UserDto userDto = new UserDto();
        // Dividir el nombre completo en nombre y apellido
        String[] nombreCompleto = user.getName().split(" ", 2);
        userDto.setFirstName(nombreCompleto.length > 0 ? nombreCompleto[0] : user.getName());
        userDto.setLastName(nombreCompleto.length > 1 ? nombreCompleto[1] : "");
        userDto.setEmail(user.getEmail());
        userDto.setPassword(password);
        
        // Usar el servicio existente para guardar el usuario
        userService.saveUser(userDto);
        
        // Obtener el usuario recién creado para asignarle el rol específico si es necesario
        User nuevoUsuario = userRepository.findByEmail(userDto.getEmail());
        
        // Si se especificó un rol diferente al predeterminado
        if (roleValue != null && !roleValue.isEmpty() && !"ROLE_USER".equals(roleValue)) {
            // Buscar el rol por nombre
            Role role = roleRepository.findByName(roleValue);
            
            // Si no se encuentra por nombre, intentar por ID
            if (role == null) {
                try {
                    Long roleId = Long.parseLong(roleValue);
                    role = roleRepository.findById(roleId).orElse(null);
                } catch (NumberFormatException e) {
                    // No es un ID válido
                }
            }
            
            // Si se encontró el rol y es diferente al rol de usuario predeterminado
            if (role != null && !role.getName().equals("ROLE_USER")) {
                // Limpiar roles existentes (que serían ROLE_USER por defecto)
                nuevoUsuario.getRoles().clear();
                // Agregar el nuevo rol
                nuevoUsuario.getRoles().add(role);
                // Guardar el usuario con el nuevo rol
                userRepository.save(nuevoUsuario);
            }
        }
        
        redirectAttributes.addFlashAttribute("success", "Usuario creado exitosamente");
        return "redirect:/admin/users";
    }

    // Método para mostrar el formulario de creación de solicitudes de prueba
    @GetMapping("/newsolicitude")
    public ModelAndView mostrarFormularioCrearSolicitud() {
        ModelAndView mv = new ModelAndView("admin/newsolicitude");
        
        // Obtener todas las organizaciones (usuarios con rol ROLE_ORGANIZATION)
        List<User> organizaciones = userRepository.findByRoleName("ROLE_ORGANIZATION");
        mv.addObject("organizaciones", organizaciones);
        
        // Obtener todos los usuarios regulares para asignar la solicitud
        List<User> usuarios = userRepository.findByRoleName("ROLE_USER");
        mv.addObject("usuarios", usuarios);
        
        // Agregar una nueva solicitud vacía al modelo
        mv.addObject("solicitud", new Solicitude());
        
        return mv;
    }

    @PostMapping("/newsolicitude")
    public String crearSolicitudPrueba(@Valid @ModelAttribute("solicitud") Solicitude solicitud,
                                   BindingResult result, RedirectAttributes msg,
                                   @RequestParam("file") MultipartFile imagen,
                                   @RequestParam("userId") Long userId) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/admin/newsolicitude";
        }

        // Establecer la fecha actual
        solicitud.setFecha(LocalDateTime.now());
        
        // Establecer activo como true
        solicitud.setActivo(true);

        // Manejar la imagen
        if (!imagen.isEmpty()) {
            try {
                byte[] bytes = imagen.getBytes();
                Path rutaImagen = Paths.get("src/main/resources/static/img/" + imagen.getOriginalFilename());
                Files.write(rutaImagen, bytes);
                solicitud.setImagen(imagen.getOriginalFilename());
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/admin/newsolicitude";
            }
        } else {
            msg.addFlashAttribute("error", "Debe seleccionar una imagen.");
            return "redirect:/admin/newsolicitude";
        }

        // Buscar y asignar el usuario seleccionado
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            solicitud.setUser(user);
            solicitudeRepository.save(solicitud);
            msg.addFlashAttribute("exito", "Solicitud de prueba creada con éxito para el usuario " + user.getName());
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario seleccionado.");
            return "redirect:/admin/newsolicitude";
        }

        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/newreport")
    public ModelAndView mostrarFormularioCrearReporte() {
        ModelAndView mv = new ModelAndView("admin/newreport");
        // Obtener usuarios con rol "ROLE_USER" para asignar el reporte
        List<User> usuarios = userRepository.findByRoleName("ROLE_USER");
        mv.addObject("usuarios", usuarios);
        mv.addObject("reporte", new Report());
        return mv;
    }

    @PostMapping("/newreport")
    public String crearReportePrueba(@ModelAttribute("reporte") Report reporte,
                                    @RequestParam("userId") Long userId,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Buscar el usuario por ID
            Optional<User> usuarioOpt = userRepository.findById(userId);
            if (usuarioOpt.isPresent()) {
                User usuario = usuarioOpt.get();
                reporte.setUser(usuario);
                
                // Guardar el reporte
                Report savedReport = reportRepository.save(reporte);
                redirectAttributes.addFlashAttribute("success", "Reporte de prueba #" + savedReport.getId() + " creado exitosamente para " + usuario.getName());
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado con ID: " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error completo en la consola
            redirectAttributes.addFlashAttribute("error", "Error al crear el reporte: " + e.getMessage());
        }
        
        return "redirect:/admin/reports";
    }
}
