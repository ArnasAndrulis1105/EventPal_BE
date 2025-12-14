package org.example.eventpal.dto.ticket.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseTicketRequest {
    @NotBlank
    @JsonProperty("reservation_id")
    private String reservationId;

    @NotBlank
    @JsonProperty("payment_intent_id")
    private String paymentIntentId;
}
