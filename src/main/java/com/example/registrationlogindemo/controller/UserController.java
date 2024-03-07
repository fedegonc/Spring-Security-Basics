package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.service.ServiceSolicitude;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {

    private final ServiceSolicitude empleosService;


    public UserController(ServiceSolicitude empleosService) {
        this.empleosService = empleosService;

    }





}
