package org.example.eventpal.repositories;

import org.example.eventpal.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType,Long> {
    List<TicketType> findByEvent_Id(Long eventId);
    boolean existsByEvent_IdAndNameIgnoreCase(Long eventId, String name);
    boolean existsByEvent_IdAndNameIgnoreCaseAndIdNot(Long eventId, String name, Long id);
}
