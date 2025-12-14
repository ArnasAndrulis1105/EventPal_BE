package org.example.eventpal.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ticket_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    @Column(nullable = false)
    private String buyerEmail;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String paymentIntentId;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();
}
