package com.tickets.dto;

import com.tickets.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketUpdateDto {
  private String subject;
  private String description;
  private String assignedTo;
  private Status status;
  private String comment;
}
