package org.example.eventpal.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.TicketStatus;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(
        name = "tickets",
        uniqueConstraints = {
                // make seat unique *within the same event*
                @UniqueConstraint(name = "uq_tickets_event_seat", columnNames = {"event_id", "seat"})
        },
        indexes = {
                @Index(name = "ix_tickets_status", columnList = "ticketStatus"),
                @Index(name = "ix_tickets_type", columnList = "ticket_type_id")
        }
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private long seat;

    @Column(nullable = false)
    private float price;                  // consider keeping as "pricePaid" snapshot on purchase

    // if this mirrors the event time, you can drop it and use event.startsAt
    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus ticketStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private TicketOrder order;   // nullable until purchased
}
