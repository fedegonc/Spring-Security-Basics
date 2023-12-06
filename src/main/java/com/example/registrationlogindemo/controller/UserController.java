package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Candidato;
import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.service.CandidatoService;
import com.example.registrationlogindemo.service.ServiceEmpleos;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final ServiceEmpleos empleosService;
    private final CandidatoService candidatoService;

    public UserController(ServiceEmpleos empleosService, CandidatoService candidatoService) {
        this.empleosService = empleosService;
        this.candidatoService = candidatoService;
    }

    @GetMapping("/postularse/{id}")
    public String mostrarFormularioPostulacion(@PathVariable int id, Model model) {
        Empleos empleo = empleosService.findById(id);
        model.addAttribute("empleo", empleo);
        model.addAttribute("candidato", new Candidato());
        return "candidatos/postulacion";
    }

    @PostMapping("/postularse/{id}")
    public String postularse(@PathVariable int id, @ModelAttribute Candidato candidato) {
        Empleos empleo = empleosService.findById(id);
        candidato.setEmpleo(empleo);
        candidatoService.guardarCandidato(candidato);
        return "redirect:/gracias";
    }


    @RequestMapping(value = "/gracias", method = RequestMethod.GET)
    public String gracias() {
        return "candidatos/gracias";
    }


}
