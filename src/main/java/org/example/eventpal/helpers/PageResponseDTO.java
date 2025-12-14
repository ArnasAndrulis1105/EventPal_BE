package org.example.eventpal.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResponseDTO<T> {
    private List<T> content;
    private long totalElements;
    private long totalPages;
    private int size;
    private int page;
}
