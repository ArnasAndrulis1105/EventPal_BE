package org.example.eventpal.helpers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageRequestDTO {
    @Min(0)
    private int page;

    @Positive
    private int size;

    private String sort;
}
