package com.tickets.repository;

import com.tickets.model.Ticket;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  public List<Ticket> findAllByAssignedTo_Id(UUID id);
}
