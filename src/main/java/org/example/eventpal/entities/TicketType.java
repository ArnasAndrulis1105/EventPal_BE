package org.example.eventpal.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(
        name = "ticket_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tickettype_event_name", columnNames = {"event_id", "name"})
        }
)
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String name;          // "General", "VIP"

    @Column(nullable = false)
    private int capacity;         // max units for this type (per event)

    @Column(nullable = false)
    private BigDecimal price;          // matches your current float usage

    @Column(nullable = false)
    private String currency = "EUR";

    // optional sales window
    @Column
    private LocalDateTime salesStart;

    @Column
    private LocalDateTime salesEnd;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private long version;
}
