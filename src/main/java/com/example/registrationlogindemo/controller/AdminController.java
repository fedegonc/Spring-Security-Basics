package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.service.ServiceSolicitude;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ServiceSolicitude empleosService;

    public AdminController(ServiceSolicitude empleosService) {
        this.empleosService = empleosService;
    }


}
