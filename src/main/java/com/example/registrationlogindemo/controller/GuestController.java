package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.ArticleService;
import com.example.registrationlogindemo.service.ReportService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class GuestController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("guest/index");;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                && authentication.getPrincipal().equals("anonymousUser"));

        if (isAuthenticated) {
            return new ModelAndView("redirect:/init");
        }

        List<Article> articles = articleService.getArticlesByCategory(Article.Categoria.GENERICO);
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/ambiental")
    public ModelAndView ambiental() {
        ModelAndView mv = new ModelAndView("guest/ambiental");

        List<Article> articles = articleService.getArticlesByCategory(Article.Categoria.EDUCACION_AMBIENTAL);
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/noticias")
    public ModelAndView noticias() {
        ModelAndView mv = new ModelAndView("guest/noticias");

        List<Article> articles = articleService.getArticlesByCategory(Article.Categoria.NOTICIA);
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/materiales")
    public ModelAndView materiales() {
        ModelAndView mv = new ModelAndView("guest/materiales");

        List<Article> articles = articleService.getArticlesByCategory(Article.Categoria.TIPOS_DE_MATERIALES);
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/alianzas")
    public ModelAndView alianzas() {
        ModelAndView mv = new ModelAndView("guest/alianzas");

        List<Article> articles = articleService.getArticlesByCategory(Article.Categoria.ALIANZAS);
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/legislacion")
    public ModelAndView legislacion() {
        ModelAndView mv = new ModelAndView("guest/legislacion");

        List<Article> articles = articleService.getArticlesByCategory(Article.Categoria.LEGISLACION);
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/report")
    public ModelAndView newReport() {
        return  new ModelAndView("user/report-problem");
    }

    @PostMapping("/report")
    public String newReportPost(
            @Valid Report report,
            BindingResult result,
            RedirectAttributes msg,
            @AuthenticationPrincipal UserDetails currentUser) {
        // Validar si hay errores en el formulario
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "El formulario contiene errores. Por favor, corrige los campos.");
            return "redirect:/report/form"; // Ajusta la ruta según sea necesario
        }

        // Obtener el usuario actual
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        if (authenticatedUserOpt.isPresent()) {
            // Asignar el usuario al reporte
            report.setUser(authenticatedUserOpt.get());

            // Guardar el reporte
            reportService.saveReport(report);

            // Agregar mensaje de éxito
            msg.addFlashAttribute("exito", "Reporte realizado con éxito.");
        } else {
            // Manejar el caso donde no se encuentra el usuario
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/init";
    }

}
