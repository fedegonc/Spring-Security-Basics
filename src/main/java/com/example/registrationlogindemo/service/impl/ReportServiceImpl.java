package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ReportService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserService userService;

    @Override
    public Report saveReport(Report report) {
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Verificar si el usuario está presente
        if (authenticatedUserOpt.isPresent()) {
            // Asignar el usuario al reporte
            report.setUser(authenticatedUserOpt.get());
        } else {
            // Manejo del caso donde no hay un usuario autenticado
            throw new IllegalStateException("No se encontró un usuario autenticado.");
        }

        // Guardar el reporte utilizando el repositorio
        return reportRepository.save(report);
    }

}
