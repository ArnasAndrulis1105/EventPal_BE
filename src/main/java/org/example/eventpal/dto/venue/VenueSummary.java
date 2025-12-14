package org.example.eventpal.dto.venue;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VenueSummary {
    private Long id;
    private String venueName;
    private String venueAddress;
    @Positive
    private int seatCount;
}
