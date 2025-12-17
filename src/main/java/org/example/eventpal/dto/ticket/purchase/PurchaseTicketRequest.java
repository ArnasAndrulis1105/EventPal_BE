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
    @JsonProperty("reservationId")
    private String reservationId;

    @NotBlank
    @JsonProperty("paymentIntentId")
    private String paymentIntentId;
}
