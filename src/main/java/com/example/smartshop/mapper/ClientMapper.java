package com.example.smartshop.mapper;

import com.example.smartshop.entity.Client;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.CustomerTier;
import com.example.smartshop.dto.ClientDTO;
import com.example.smartshop.dto.ClientResponseDTO;
import com.example.smartshop.dto.CreateClientDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDTO toDTO(Client client) {
        if (client == null) {
            return null;
        }

        return ClientDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .tier(client.getTier() != null ? client.getTier().name() : "BASIC")
                .totalOrders(client.getTotalOrders() != null ? client.getTotalOrders() : 0)
                .totalSpent(client.getTotalSpent() != null ? client.getTotalSpent() : 0.0)
                .build();
    }

    public ClientResponseDTO toResponseDTO(Client client) {
        if (client == null) {
            return null;
        }

        return ClientResponseDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .tier(client.getTier())
                .totalOrders(0)
                .totalSpent(0.0)
                .build();
    }

    public Client toEntity(CreateClientDTO dto) {
        if (dto == null) {
            return null;
        }

        return Client.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(0.0)
                .build();
    }

    public Client updateEntityWithUser(Client client, User user) {
        if (client != null && user != null) {
            client.setUser(user);
        }
        return client;
    }
}