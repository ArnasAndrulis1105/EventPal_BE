package org.example.eventpal.controllers;

import jakarta.validation.Valid;
import org.example.eventpal.dto.ticket.TicketResponse;
import org.example.eventpal.dto.ticket.purchase.PurchaseTicketRequest;
import org.example.eventpal.dto.ticket.purchase.PurchaseTicketResponse;
import org.example.eventpal.dto.ticket.reservation.ReservationTicketRequest;
import org.example.eventpal.dto.ticket.reservation.TicketReservationResponse;
import org.example.eventpal.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final OrderService service;

    public CheckoutController(OrderService service) {
        this.service = service;
    }

    @PostMapping(value = "/reservations", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<TicketReservationResponse> reserve(
            @Valid @RequestBody ReservationTicketRequest req,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal
    ) {
        var resp = service.reserve(req, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping(value = "/purchases", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('BUYER')")
    public PurchaseTicketResponse purchase(
            @Valid @RequestBody PurchaseTicketRequest req,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal
    ) {
        return service.purchase(req, principal.getUsername());
    }


    @GetMapping(value = "/orders/{orderNumber}", produces = "application/json")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PurchaseTicketResponse> getOrder(@PathVariable String orderNumber) {
        return service.getOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/myTickets", produces = "application/json")
    @PreAuthorize("hasRole('BUYER')")
    public List<TicketResponse> myTickets(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal
    ) {
        return service.myTickets(principal.getUsername()); // username == email in your User.getUsername()
    }

    @GetMapping(value = "/myReservations", produces = "application/json")
    @PreAuthorize("hasRole('BUYER')")
    public List<TicketReservationResponse> myReservations(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal
    ) {
        return service.myReservations(principal.getUsername());
    }


}
