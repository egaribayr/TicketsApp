package com.tickets.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "tickets")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String subject;
  private String description;

  @OneToOne private User createdBy;

  @OneToOne private User modifiedBy;

  @OneToOne private User assignedTo;

  private Date createdAt;
  private Date modifiedAt;

  @Enumerated(EnumType.STRING)
  private Status status;

  @OneToMany private List<TicketHistory> ticketHistory;
}
