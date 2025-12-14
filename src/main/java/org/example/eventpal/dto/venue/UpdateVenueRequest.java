package org.example.eventpal.dto.venue;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVenueRequest {

    private String venueName;
    private String venueAddress;
    @Positive
    private int seatCount;
}
