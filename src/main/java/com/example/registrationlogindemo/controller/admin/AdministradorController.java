package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdministradorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolicitudeRepository solicitudeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");
        return mv;
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
        ModelAndView mv = new ModelAndView("user/profile");
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
            }
            mv.addObject("user", user);
            mv.addObject("organizationTypes", OrganizationType.values());
        }

        return mv;
    }

    @PostMapping("/profile/{id}")
    public ModelAndView editUser(@ModelAttribute("user") @Valid User user,
                                 BindingResult result,
                                 @PathVariable("id") long id,
                                 @RequestParam("fileImage") MultipartFile fileImage,
                                 @RequestParam("currentProfileImageUrl") String currentProfileImageUrl,
                                 @RequestParam(value = "organizationType", required = false) String organizationType,
                                 @RequestParam(value = "organizationName", required = false) String organizationName,
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

            // Actualizar tipo y nombre de organización
            if (organizationType != null && !organizationType.isEmpty()) {
                userEdit.setOrganizationType(OrganizationType.valueOf(organizationType));
            }
            userEdit.setOrganizationName(organizationName);

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

        List<Report> reports = reportRepository.findAll();
        mv.addObject("reports", reports);
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

    @GetMapping("/roles")
    public ModelAndView rootRoles() {
        ModelAndView mv = new ModelAndView("admin/roles");
        List<Role> roles = roleRepository.findAll();

        // Agregar conteo de usuarios por rol
        Map<Long, Integer> roleUserCounts = new HashMap<>();
        for (Role role : roles) {
            // Consultar cuántos usuarios tienen este rol
            int userCount = userRepository.countByRolesId(role.getId());
            roleUserCounts.put(role.getId(), userCount);
        }

        mv.addObject("roles", roles);
        mv.addObject("roleUserCounts", roleUserCounts);

        return mv;
    }

    @GetMapping("/editrole/{id}")
    public ModelAndView adminEditReportrole(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/roles");
        Optional<Role> role = roleRepository.findById(id);
        if (role.isPresent()) {
            // Obtener el conteo de usuarios por rol para mantener la consistencia
            List<Role> roles = roleRepository.findAll();
            Map<Long, Integer> roleUserCounts = new HashMap<>();
            for (Role r : roles) {
                int userCount = userRepository.countByRolesId(r.getId());
                roleUserCounts.put(r.getId(), userCount);
            }

            mv.addObject("roles", roles);
            mv.addObject("roleUserCounts", roleUserCounts);
            mv.addObject("editingRole", role.get());
            mv.addObject("isEditing", true);
        } else {
            mv.setViewName("redirect:/admin/roles");
            mv.addObject("error", "El rol que intenta editar no existe.");
        }
        return mv;
    }

    // Método para procesar la actualización de un rol
    @PostMapping("/updaterole")
    public String updateRole(@RequestParam("id") Long id,
                             @RequestParam("roleName") String roleName,
                             RedirectAttributes redirectAttributes) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();

            // Verificar si el nombre ya existe en otro rol
            Role existingRole = roleRepository.findByName(roleName);
            if (existingRole != null && !existingRole.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un rol con el nombre '" + roleName + "'.");
                return "redirect:/admin/roles";
            }

            // Actualizar el rol
            role.setName(roleName);
            roleRepository.save(role);
            redirectAttributes.addFlashAttribute("success", "Rol actualizado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró el rol para actualizar.");
        }

        return "redirect:/admin/roles";
    }

    // Método para eliminar un rol
    @GetMapping("/deleterole/{id}")
    public String deleteRole(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        // Verificar si hay usuarios con este rol antes de eliminarlo
        int userCount = userRepository.countByRolesId(id);
        if (userCount > 0) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar este rol porque hay " + userCount + " usuario(s) asociado(s) a él.");
            return "redirect:/admin/roles";
        }

        try {
            roleRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Rol eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el rol: " + e.getMessage());
        }

        return "redirect:/admin/roles";
    }

    // Método para crear un nuevo rol desde la lista desplegable
    @PostMapping("/newrole")
    public String createNewRole(@RequestParam("roleName") String roleName, RedirectAttributes redirectAttributes) {
        // Verificar si el rol ya existe
        Role existingRole = roleRepository.findByName(roleName);
        if (existingRole != null) {
            redirectAttributes.addFlashAttribute("error", "El rol '" + roleName + "' ya existe.");
            return "redirect:/admin/roles";
        }

        // Crear y guardar el nuevo rol
        Role newRole = new Role();
        newRole.setName(roleName);
        roleRepository.save(newRole);

        redirectAttributes.addFlashAttribute("success", "Rol '" + roleName + "' creado exitosamente.");
        return "redirect:/admin/roles";
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
            // Agregar los tipos de organización al modelo
            mv.addObject("organizationTypes", OrganizationType.values());
        }
        return mv;
    }

    // Método para procesar la edición de un usuario
    @PostMapping("/edit/{id}")
    public String adminEditUserBanco(@ModelAttribute("User") @Valid User user,
                                    @RequestParam(value = "organizationType", required = false) String organizationType,
                                    @RequestParam(value = "organizationName", required = false) String organizationName,
                                    BindingResult result, RedirectAttributes msg) {
        // Verificar errores de validación
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/editar/" + user.getId();
        }
        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            // Actualizar los datos del usuario con los nuevos valores
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            userEdit.setRoles(user.getRoles());
            
            // Actualizar tipo y nombre de organización
            if (organizationType != null && !organizationType.isEmpty()) {
                userEdit.setOrganizationType(OrganizationType.valueOf(organizationType));
                userEdit.setOrganizationName(organizationName);
            } else {
                // Si no se selecciona un tipo de organización, establecer ambos campos como nulos
                userEdit.setOrganizationType(null);
                userEdit.setOrganizationName(null);
            }
            
            // Guardar los cambios en la base de datos
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró el usuario a editar.");
        }

        return "redirect:/admin/dashboard";
    }

    // Método para eliminar una solicitud
    @GetMapping("/deletuser/{id}")
    public String adminExcluirUser(@PathVariable("id") int id) {
        userService.deleteUserById((long) id);
        return "redirect:/admin/dashboard";
    }

    // Método para mostrar las solicitudes de organizaciones pendientes de aprobación
    @GetMapping("/organizations/pending")
    public ModelAndView pendingOrganizations() {
        ModelAndView mv = new ModelAndView("admin/pending-organizations");
        List<Organization> organizations = organizationService.getOrganizationsByStatus("PENDING");
        mv.addObject("organizations", organizations);
        return mv;
    }

    // Método para aprobar una organización
    @GetMapping("/organizations/approve/{id}")
    public String approveOrganization(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // Actualizar estado de la organización a APPROVED
        organizationService.updateOrganizationStatus(id, "APPROVED");

        // Obtener la organización actualizada
        Optional<Organization> orgOptional = organizationService.getOrganizationById(id);
        if (orgOptional.isPresent()) {
            // Obtener el propietario y añadir rol de ORGANIZATION
            Organization org = orgOptional.get();
            User owner = org.getOwner();
            Role organizationRole = roleRepository.findByName("ROLE_ORGANIZATION");

            if (organizationRole != null && !owner.getRoles().contains(organizationRole)) {
                owner.getRoles().add(organizationRole);
                userRepository.save(owner);
            }
        }

        redirectAttributes.addFlashAttribute("success", "Organización aprobada exitosamente");
        return "redirect:/admin/organizations/pending";
    }

    // Método para rechazar una organización
    @GetMapping("/organizations/reject/{id}")
    public String rejectOrganization(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // Actualizar estado de la organización a REJECTED
        organizationService.updateOrganizationStatus(id, "REJECTED");
        redirectAttributes.addFlashAttribute("success", "Organización rechazada");
        return "redirect:/admin/organizations/pending";
    }
}
