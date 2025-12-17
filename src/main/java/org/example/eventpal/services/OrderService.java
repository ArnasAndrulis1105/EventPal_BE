package org.example.eventpal.services;

import jakarta.transaction.Transactional;
import org.example.eventpal.dto.ticket.TicketResponse;
import org.example.eventpal.dto.ticket.purchase.PurchaseTicketRequest;
import org.example.eventpal.dto.ticket.purchase.PurchaseTicketResponse;
import org.example.eventpal.dto.ticket.reservation.ReservationLineItem;
import org.example.eventpal.dto.ticket.reservation.ReservationTicketRequest;
import org.example.eventpal.dto.ticket.reservation.TicketReservationResponse;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.entities.Event;
import org.example.eventpal.entities.Ticket;
import org.example.eventpal.entities.TicketOrder;
import org.example.eventpal.entities.TicketType;
import org.example.eventpal.enumerators.TicketStatus;
import org.example.eventpal.helpers.IdNameDTO;
import org.example.eventpal.helpers.MoneyDTO;
import org.example.eventpal.repositories.EventRepository;
import org.example.eventpal.repositories.TicketOrderRepository;
import org.example.eventpal.repositories.TicketRepository;
import org.example.eventpal.repositories.TicketTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class OrderService {

    private final EventRepository eventRepo;
    private final TicketTypeRepository ticketTypeRepo;
    private final TicketRepository ticketRepo;
    private final TicketOrderRepository orderRepo;

    // In-memory reservations (demo). For prod: Redis or DB.
    private final Map<String, ReservationData> reservations = new ConcurrentHashMap<>();

    public OrderService(EventRepository eventRepo,
                        TicketTypeRepository ticketTypeRepo,
                        TicketRepository ticketRepo,
                        TicketOrderRepository orderRepo) {
        this.eventRepo = eventRepo;
        this.ticketTypeRepo = ticketTypeRepo;
        this.ticketRepo = ticketRepo;
        this.orderRepo = orderRepo;
    }

    // ------------------ RESERVE ------------------

    public TicketReservationResponse reserve(ReservationTicketRequest req, String buyerEmail) {
        Event event = eventRepo.findById(req.getEventId())
                .orElseThrow(() -> notFound("Event", req.getEventId()));

        TicketType type = ticketTypeRepo.findById(req.getTicketTypeId())
                .orElseThrow(() -> notFound("TicketType", req.getTicketTypeId()));

        if (req.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        int issuedForType = (int) ticketRepo.countByTicketType_Id(type.getId());
        int remaining = Math.max(0, type.getCapacity() - issuedForType);
        if (req.getQuantity() > remaining) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough capacity for this ticket type");
        }

        MoneyDTO unit = MoneyDTO.builder()
                .currency(type.getCurrency())
                .price(type.getPrice().setScale(2, RoundingMode.HALF_UP))
                .build();

        BigDecimal lineTotal = unit.getPrice()
                .multiply(BigDecimal.valueOf(req.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        MoneyDTO total = MoneyDTO.builder().currency(unit.getCurrency()).price(lineTotal).build();

        String reservationId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        reservations.put(reservationId, new ReservationData(
                reservationId, expiresAt,
                event.getId(), type.getId(), req.getQuantity(),
                unit.getCurrency(), unit.getPrice(),
                buyerEmail // <- now comes from auth
        ));

    var item = ReservationLineItem.builder()
                .ticketTypeId(type.getId())
                .ticketTypeName(type.getName())
                .quantity(req.getQuantity())
                .unitPrice(unit)
                .lineTotal(total)
                .build();

        return TicketReservationResponse.builder()
                .reservationId(reservationId)
                .expiresAt(expiresAt)
                .event(IdNameDTO.builder().id(event.getId()).name(event.getName()).build())
                .items(List.of(item))
                .total(total)
                .build();
    }

    // ------------------ PURCHASE ------------------

    public PurchaseTicketResponse purchase(PurchaseTicketRequest req, String principalEmail) {
        ReservationData data = reservations.get(req.getReservationId());
        if (data == null || data.expiresAt.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reservation expired or not found");
        }

        if (!data.buyerEmail.equalsIgnoreCase(principalEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to you");
        }

        // Idempotency: if order exists for this paymentIntent, return it
        Optional<TicketOrder> existingByPayment = orderRepo.findByPaymentIntentId(req.getPaymentIntentId());
        if (existingByPayment.isPresent()) {
            TicketOrder ord = existingByPayment.get();
            List<Ticket> tickets = ticketRepo.findAllByOrder_Id(ord.getId());
            return PurchaseTicketResponse.builder()
                    .orderNumber(ord.getOrderNumber())
                    .purchasedAt(ord.getPurchasedAt())
                    .tickets(tickets.stream().map(this::toTicketResponse).toList())
                    .totalCharged(MoneyDTO.builder().currency(ord.getCurrency()).price(ord.getTotalAmount()).build())
                    .buyerEmail(ord.getBuyerEmail())
                    .build();
        }

        Event event = eventRepo.findById(data.eventId)
                .orElseThrow(() -> notFound("Event", data.eventId));
        TicketType type = ticketTypeRepo.findById(data.ticketTypeId)
                .orElseThrow(() -> notFound("TicketType", data.ticketTypeId));

        // Strict capacity check right before issuing
        int issuedForType = (int) ticketRepo.countByTicketType_Id(type.getId());
        int remaining = Math.max(0, type.getCapacity() - issuedForType);
        if (data.quantity > remaining) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Capacity changed; not enough tickets left");
        }

        // Create tickets
        List<Ticket> toSave = new ArrayList<>();
        Set<Long> seatsPicked = new HashSet<>();

        for (int i = 0; i < data.quantity; i++) {
            long seat = pickSeat(event.getId(), seatsPicked);
            seatsPicked.add(seat);

            Ticket t = Ticket.builder()
                    .description(type.getName())
                    .seat(seat)
                    .price(data.unitAmount.floatValue())
                    .dateTime(event.getStartsAt())
                    .ticketStatus(TicketStatus.SOLD)
                    .event(event)
                    .ticketType(type)
                    .build();
            toSave.add(t);
        }

        List<Ticket> saved = ticketRepo.saveAll(toSave);


        // Create order and attach tickets
        BigDecimal total = data.unitAmount.multiply(BigDecimal.valueOf(data.quantity)).setScale(2, RoundingMode.HALF_UP);
        String orderNo = "ORD-" + req.getReservationId();
        TicketOrder order = TicketOrder.builder()
                .orderNumber(orderNo)
                .purchasedAt(LocalDateTime.now())
                .buyerEmail(data.buyerEmail)
                .currency(data.currency)
                .totalAmount(total)
                .paymentIntentId(req.getPaymentIntentId())
                .build();
        order = orderRepo.save(order);

        for (Ticket t : saved) {
            t.setOrder(order);
        }
        // JPA dirty checking will persist the relation
        reservations.remove(req.getReservationId());

        return PurchaseTicketResponse.builder()
                .orderNumber(orderNo)
                .purchasedAt(order.getPurchasedAt())
                .tickets(saved.stream().map(this::toTicketResponse).toList())
                .totalCharged(MoneyDTO.builder().currency(order.getCurrency()).price(order.getTotalAmount()).build())
                .buyerEmail(order.getBuyerEmail())
                .build();
    }

    // ------------------ READ (optional helpers) ------------------

    public Optional<PurchaseTicketResponse> getOrderByNumber(String orderNumber) {
        return orderRepo.findByOrderNumber(orderNumber).map(ord -> {
            List<Ticket> tickets = ticketRepo.findAllByOrder_Id(ord.getId());
            return PurchaseTicketResponse.builder()
                    .orderNumber(ord.getOrderNumber())
                    .purchasedAt(ord.getPurchasedAt())
                    .tickets(tickets.stream().map(this::toTicketResponse).toList())
                    .totalCharged(MoneyDTO.builder().currency(ord.getCurrency()).price(ord.getTotalAmount()).build())
                    .buyerEmail(ord.getBuyerEmail())
                    .build();
        });
    }

    // ------------------ local helpers ------------------

    private TicketResponse toTicketResponse(Ticket t) {
        TicketType type = t.getTicketType();
        TicketTypeResponse typeDto = TicketTypeResponse.builder()
                .id(type.getId())
                .event(IdNameDTO.builder().id(t.getEvent().getId()).name(t.getEvent().getName()).build())
                .name(type.getName())
                .capacity(type.getCapacity())
                .price(MoneyDTO.builder()
                        .currency(type.getCurrency())
                        .price(type.getPrice().setScale(2, RoundingMode.HALF_UP))
                        .build())
                .salesStart(type.getSalesStart())
                .salesEnd(type.getSalesEnd())
                .active(type.isActive())
                .version(type.getVersion())
                .build();

        return TicketResponse.builder()
                .id(t.getId())
                .description(t.getDescription())
                .seat(t.getSeat())
                .pricePaid(MoneyDTO.builder()
                        .currency(type.getCurrency())
                        .price(new BigDecimal(Float.toString(t.getPrice())).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .dateTime(t.getDateTime())
                .ticketStatus(t.getTicketStatus())
                .event(IdNameDTO.builder().id(t.getEvent().getId()).name(t.getEvent().getName()).build())
                .ticketType(typeDto)
                .build();
    }

    private long pickSeat(Long eventId, Set<Long> alreadyPicked) {
        long seat = 1;
        while (alreadyPicked.contains(seat) || ticketRepo.existsByEvent_IdAndSeat(eventId, seat)) {
            seat++;
        }
        return seat;
    }

    public List<TicketResponse> myTickets(String buyerEmail) {
        return ticketRepo.findAllByOrder_BuyerEmailOrderByDateTimeDesc(buyerEmail)
                .stream()
                .map(this::toTicketResponse)
                .toList();
    }

    public List<TicketReservationResponse> myReservations(String buyerEmail) {
        LocalDateTime now = LocalDateTime.now();

        return reservations.values().stream()
                .filter(r -> r.expiresAt.isAfter(now))
                .filter(r -> Objects.equals(r.buyerEmail, buyerEmail))
                .map(r -> {
                    Event event = eventRepo.findById(r.eventId)
                            .orElseThrow(() -> notFound("Event", r.eventId));
                    TicketType type = ticketTypeRepo.findById(r.ticketTypeId)
                            .orElseThrow(() -> notFound("TicketType", r.ticketTypeId));

                    MoneyDTO unit = MoneyDTO.builder().currency(r.currency).price(r.unitAmount).build();

                    BigDecimal lineTotal = r.unitAmount
                            .multiply(BigDecimal.valueOf(r.quantity))
                            .setScale(2, RoundingMode.HALF_UP);

                    MoneyDTO total = MoneyDTO.builder().currency(r.currency).price(lineTotal).build();

                    var item = ReservationLineItem.builder()
                            .ticketTypeId(type.getId())
                            .ticketTypeName(type.getName())
                            .quantity(r.quantity)
                            .unitPrice(unit)
                            .lineTotal(total)
                            .build();

                    return TicketReservationResponse.builder()
                            .reservationId(r.reservationId)
                            .expiresAt(r.expiresAt)
                            .event(IdNameDTO.builder().id(event.getId()).name(event.getName()).build())
                            .items(List.of(item))
                            .total(total)
                            .build();
                })
                .toList();
    }




    private static ResponseStatusException notFound(String type, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found: " + id);
    }

    // reservation snapshot (inner class – not a helper file)
    private static class ReservationData {
        final String reservationId;
        final LocalDateTime expiresAt;
        final Long eventId;
        final Long ticketTypeId;
        final int quantity;
        final String currency;
        final BigDecimal unitAmount;
        final String buyerEmail;

        ReservationData(String reservationId, LocalDateTime expiresAt, Long eventId, Long ticketTypeId, int quantity,
                        String currency, BigDecimal unitAmount, String buyerEmail) {
            this.reservationId = reservationId;
            this.expiresAt = expiresAt;
            this.eventId = eventId;
            this.ticketTypeId = ticketTypeId;
            this.quantity = quantity;
            this.currency = currency;
            this.unitAmount = unitAmount;
            this.buyerEmail = buyerEmail;
        }
    }
}
