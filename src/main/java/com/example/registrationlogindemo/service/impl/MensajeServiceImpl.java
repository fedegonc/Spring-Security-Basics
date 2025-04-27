package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Mensaje;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.MensajeRepository;
import com.example.registrationlogindemo.service.MensajeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;

    public MensajeServiceImpl(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Override
    public List<Mensaje> findMessagesBySolicitudeAndUser(Solicitude solicitude, User currentUserEntity) {
        // Obtener todos los mensajes por solicitud
        List<Mensaje> messagesBySolicitude = mensajeRepository.findBySolicitude(solicitude);

        // Filtrar los mensajes por usuario
        List<Mensaje> filteredMessages = messagesBySolicitude.stream()
                .filter(message -> message.getUser().equals(currentUserEntity))
                .collect(Collectors.toList());

        return filteredMessages;
    }

    @Override
    public List<Mensaje> findMessagesBySolicitude(Solicitude solicitude) {
        return mensajeRepository.findBySolicitude(solicitude);
    }
}
