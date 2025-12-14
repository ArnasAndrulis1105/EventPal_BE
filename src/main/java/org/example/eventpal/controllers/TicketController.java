package org.example.eventpal.controllers;

import jakarta.validation.Valid;
import org.example.eventpal.dto.ticket.TicketResponse;
import org.example.eventpal.dto.ticket.create.CreateTicketRequest;
import org.example.eventpal.enumerators.TicketStatus;
import org.example.eventpal.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // READ
    @GetMapping(value = "/{id}", produces = "application/json")
    public TicketResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // LIST by event
    @GetMapping(value = "/by-event/{eventId}", produces = "application/json")
    public List<TicketResponse> listByEvent(@PathVariable Long eventId) {
        return service.listByEvent(eventId);
    }

    // PATCH status (e.g., AVAILABLE -> CANCELED)
    @PatchMapping(value = "/{id}/status", produces = "application/json")
    public TicketResponse updateStatus(@PathVariable Long id, @RequestParam("value") TicketStatus status) {
        return service.updateStatus(id, status);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
