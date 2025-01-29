package com.cauim.repository;

import com.cauim.model.EventTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTicketRepository extends JpaRepository<EventTicket, Long> {
}