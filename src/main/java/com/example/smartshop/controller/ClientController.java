package com.example.smartshop.controller;

import com.example.smartshop.dto.ClientDTO;
import com.example.smartshop.dto.CreateClientDTO;
import com.example.smartshop.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllClients() {
        List<ClientDTO> clients = clientService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Liste des clients récupérée avec succès");
        response.put("clients", clients);
        response.put("total", clients.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getClientById(@PathVariable Long id) {
        ClientDTO client = clientService.findById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Client récupéré avec succès");
        response.put("client", client);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createClient(@Valid @RequestBody CreateClientDTO dto) {
        ClientDTO client = clientService.create(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Client créé avec succès");
        response.put("client", client);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody CreateClientDTO dto) {
        ClientDTO client = clientService.update(id, dto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Client mis à jour avec succès");
        response.put("client", client);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Client supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}
