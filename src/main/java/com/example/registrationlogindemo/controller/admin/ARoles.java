package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ARoles {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    UserService userService;
    @Autowired
    ImageService imageService;



    @GetMapping("/roles")
    public ModelAndView rootRoles() {
        ModelAndView mv = new ModelAndView("admin/roles");
        List<Role> roles = roleRepository.findAll();
        mv.addObject("roles", roles);

        return mv;
    }

    @GetMapping("/editerole/{id}")
    public ModelAndView adminEditReport(@PathVariable("id") long id) {
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

}
