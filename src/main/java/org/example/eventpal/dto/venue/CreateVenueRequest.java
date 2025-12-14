package org.example.eventpal.dto.venue;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class CreateVenueRequest {

    @JsonProperty("venueName")
    @NotEmpty(message = "Name cannot be found")
    private String name;

    @JsonProperty("venueAddress")
    @NotEmpty(message = "Address cannot be found")
    private String address;

    @Positive
    @JsonProperty("seatCount")
    private int seatCount;
}
