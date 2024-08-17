package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.MessageRepository;
import com.example.registrationlogindemo.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Message> findMessagesBySolicitudeAndUser(Solicitude solicitude, User currentUserEntity) {
        // Obtener todos los mensajes por solicitud
        List<Message> messagesBySolicitude = messageRepository.findBySolicitud(solicitude);

        // Filtrar los mensajes por usuario
        List<Message> filteredMessages = messagesBySolicitude.stream()
                .filter(message -> message.getUser().equals(currentUserEntity))
                .collect(Collectors.toList());

        return filteredMessages;
    }

    @Override
    public List<Message> findMessagesBySolicitude(Solicitude solicitude) {
        return messageRepository.findBySolicitud(solicitude);
    }
}

