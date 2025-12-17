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
    @JsonProperty("eventId")
    private Long eventId;

    @NotNull
    @JsonProperty("ticketTypeId")
    private Long ticketTypeId;

    @Positive
    @JsonProperty("quantity")
    private int quantity;

    @Email
    @JsonProperty("buyerEmail")
    private String buyerEmail;
}
