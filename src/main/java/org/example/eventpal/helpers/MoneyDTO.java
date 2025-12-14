package org.example.eventpal.helpers;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MoneyDTO {
    @NotBlank
    private String currency;

    @DecimalMin("0.00")
    private BigDecimal price;
}
