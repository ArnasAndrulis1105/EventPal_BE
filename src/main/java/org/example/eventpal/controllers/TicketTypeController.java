package org.example.eventpal.controllers;

import jakarta.validation.Valid;
import org.example.eventpal.dto.ticketType.CreateTicketTypeRequest;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.dto.ticketType.UpdateTicketTypeRequest;
import org.example.eventpal.services.TicketTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ticket-types")
public class TicketTypeController {

    private final TicketTypeService service;

    public TicketTypeController(TicketTypeService service) {
        this.service = service;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TicketTypeResponse> create(@Valid @RequestBody CreateTicketTypeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public TicketTypeResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping(value = "/by-event/{eventId}", produces = "application/json")
    public List<TicketTypeResponse> listByEvent(@PathVariable Long eventId) {
        return service.listByEvent(eventId);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public TicketTypeResponse put(@PathVariable Long id, @Valid @RequestBody UpdateTicketTypeRequest req) {
        return service.update(id, req);
    }

    @PatchMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public TicketTypeResponse patch(@PathVariable Long id, @RequestBody UpdateTicketTypeRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
