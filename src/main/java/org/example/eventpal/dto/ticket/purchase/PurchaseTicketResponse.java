package org.example.eventpal.dto.ticket.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.dto.ticket.TicketResponse;
import org.example.eventpal.helpers.MoneyDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTicketResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDateTime purchasedAt;
    private List<TicketResponse> tickets;
    private MoneyDTO totalCharged;
    private String buyerEmail;
}
