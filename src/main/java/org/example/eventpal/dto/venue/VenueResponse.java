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
public class VenueResponse {
    private Long id;
    @Positive
    private int seatCount;
    private String address;
    private String name;
}
