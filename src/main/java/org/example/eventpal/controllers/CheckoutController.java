package org.example.eventpal.controllers;

import jakarta.validation.Valid;
import org.example.eventpal.dto.ticket.purchase.PurchaseTicketRequest;
import org.example.eventpal.dto.ticket.purchase.PurchaseTicketResponse;
import org.example.eventpal.dto.ticket.reservation.ReservationTicketRequest;
import org.example.eventpal.dto.ticket.reservation.TicketReservationResponse;
import org.example.eventpal.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final OrderService service;

    public CheckoutController(OrderService service) {
        this.service = service;
    }

    @PostMapping(value = "/reservations", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TicketReservationResponse> reserve(@Valid @RequestBody ReservationTicketRequest req) {
        var resp = service.reserve(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping(value = "/purchases", consumes = "application/json", produces = "application/json")
    public PurchaseTicketResponse purchase(@Valid @RequestBody PurchaseTicketRequest req) {
        return service.purchase(req);
    }

    @GetMapping(value = "/orders/{orderNumber}", produces = "application/json")
    public ResponseEntity<PurchaseTicketResponse> getOrder(@PathVariable String orderNumber) {
        return service.getOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
