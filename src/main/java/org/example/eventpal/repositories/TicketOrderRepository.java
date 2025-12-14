package org.example.eventpal.repositories;

import jakarta.validation.constraints.NotBlank;
import org.example.eventpal.entities.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketOrderRepository extends JpaRepository<TicketOrder,Long> {
    Optional<TicketOrder> findByPaymentIntentId(String paymentIntentId);

    Optional<TicketOrder> findByOrderNumber(String orderNumber);
}
