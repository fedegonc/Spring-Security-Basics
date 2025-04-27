package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Mensaje;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;

import java.util.List;

public interface MensajeService {

    List<Mensaje> findMessagesBySolicitudeAndUser(Solicitude solicitude, User currentUserEntity);

    List<Mensaje> findMessagesBySolicitude(Solicitude solicitude);
}
