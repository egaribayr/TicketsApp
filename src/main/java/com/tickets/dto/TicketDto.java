package com.tickets.dto;

import com.tickets.model.Status;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {

  private UUID id;

  private String subject;
  private String description;

  private String createdBy;
  private String modifiedBy;
  private String assignedTo;

  private Date createdAt;
  private Date modifiedAt;

  private Status status;
}
