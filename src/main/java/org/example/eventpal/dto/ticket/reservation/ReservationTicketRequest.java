package org.example.eventpal.dto.ticket.reservation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTicketRequest {
    @NotNull
    @JsonProperty("event_id")
    private Long eventId;

    @NotNull
    @JsonProperty("ticket_type_id")
    private Long ticketTypeId;

    @Positive
    @JsonProperty("quantity")
    private int quantity;

    @Email
    @JsonProperty("buyer_email")
    private String buyerEmail;
}
