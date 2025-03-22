package com.example.registrationlogindemo.controller.admin;
import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdministradorController {
    private int id;

    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");
        return mv;
    }
    @Autowired
    private UserRepository userRepository;


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

        }

        return mv;
    }

    @PostMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id,
                                 @ModelAttribute("user") @Valid User user,
                                 BindingResult result,
                                 @RequestParam("file") MultipartFile fileImage,
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
                        Path path = Paths.get("./src/main/resources/static/img/" + modifiedFilename);
                        Files.write(path, bytes);
                        userEdit.setProfileImage(modifiedFilename);
                    }
                } else {
                    // Mantener la imagen de perfil actual si no se proporciona una nueva
                    userEdit.setProfileImage(currentProfileImageUrl);
                }
            } catch (IOException e) {
                System.out.println("Error al procesar la imagen de perfil: " + e.getMessage());
                msg.addFlashAttribute("error", "Error al cargar la imagen. Inténtalo nuevamente.");
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

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    UserService userService;




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
        mv.addObject("roles", roles);

        return mv;
    }

    @GetMapping("/editerole/{id}")
    public ModelAndView adminEditReportrole(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/editrole");
        Optional<Report> image = reportRepository.findById(id);
        if (image.isPresent()) {
            mv.addObject("role", image.get());
        } else {
            mv.setViewName("redirect:/admin/dashboard");
        }
        return mv;
    }

    // Método para eliminar un reporte
    @GetMapping("/deleterole/{id}")
    public String adminExcluirReport(@PathVariable("id") int id) {
        roleRepository.deleteById((long) id);
        return "redirect:/admin/dashboard";
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
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen: " + e.getMessage());
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
    public String adminEditUserBanco(@ModelAttribute("User") @Valid User user,
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
}
