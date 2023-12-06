package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Candidato;
import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.service.CandidatoService;
import com.example.registrationlogindemo.service.ServiceEmpleos;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ServiceEmpleos empleosService;
    private final CandidatoService candidatoService;

    public AdminController(ServiceEmpleos empleosService, CandidatoService candidatoService) {
        this.empleosService = empleosService;
        this.candidatoService = candidatoService;
    }

    @GetMapping("/vercandidatos/{id}")
    public String verCandidatos(@PathVariable int id, Model model) {
        Empleos empleo = empleosService.findById(id);
        List<Candidato> candidatos = candidatoService.obtenerCandidatosPorEmpleo(empleo.getId());
        model.addAttribute("empleo", empleo);
        model.addAttribute("candidatos", candidatos);
        return "vercandidatos";
    }

    @RequestMapping(value = "/listarcandidatos", method = RequestMethod.GET)
    public ModelAndView getListarEmpleos() {
        ModelAndView mv = new ModelAndView("candidatos/vercandidatos");
        List<Candidato> candidatos = candidatoService.findAll();
        mv.addObject("candidatos", candidatos);
        return mv;
    }
}
