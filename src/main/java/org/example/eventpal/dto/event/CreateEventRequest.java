package org.example.eventpal.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    @NotEmpty(message = "Name cannot be found")
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("starts_at")
    private LocalDateTime startsAt;

    @NotNull
    @JsonProperty("venue_id")
    private Long venueId;

    @JsonProperty("description")
    private String description;
}
