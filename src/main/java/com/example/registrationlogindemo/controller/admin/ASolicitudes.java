package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ASolicitudes {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;




    private UserService userService;


    // Constructor que inyecta el servicio UserService
    public void ASolicitudes(UserService userService, ImageService imageService) {
        this.userService = userService;

    }
    @GetMapping("/solicitudes")
    public ModelAndView adminSolicitudes() {
        ModelAndView mv = new ModelAndView("root/solicitudes");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);
        return mv;
    }
    // Método para mostrar el formulario de edición de solicitud
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView adminEditSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
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
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/editar/" + solicitude.getId();
        }
        // Obtener la solicitud que se va a editar
        Solicitude changeSolicitude = solicitudeRepository.findById(solicitude.getId()).orElse(null);
        if (changeSolicitude != null) {
            // Actualizar los campos de la solicitud con los nuevos valores

            changeSolicitude.setCategoria(solicitude.getCategoria());

            changeSolicitude.setDescripcion(solicitude.getDescripcion());

            try {
                // Guardar la imagen si se proporciona una
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen");
            }
            // Guardar la solicitud editada en la base de datos
            solicitudeRepository.save(changeSolicitude);
            // Redirigir con un mensaje de éxito
            msg.addFlashAttribute("sucesso", "Alimento editado com sucesso.");
        }

        return "redirect:/dashboard";
    }
    // Método para eliminar una solicitud
    @GetMapping("/deletsolicitude/{id}")
    public String adminExcluirSolicitud(@PathVariable("id") int id) {
        solicitudeRepository.deleteSolicitudeById((long) id);
        return "redirect:/admin/dashboard";
    }
}
