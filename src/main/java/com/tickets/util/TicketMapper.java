package com.tickets.util;

import com.tickets.dto.TicketCreationDto;
import com.tickets.dto.TicketDto;
import com.tickets.dto.TicketDto.TicketDtoBuilder;
import com.tickets.dto.TicketHistoryDto;
import com.tickets.dto.TicketUpdateDto;
import com.tickets.model.ChangeType;
import com.tickets.model.Status;
import com.tickets.model.Ticket;
import com.tickets.model.TicketHistory;
import com.tickets.model.User;
import com.tickets.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Mapper service for converting between ticket-related DTOs and entities.
 *
 * <p>Handles mapping for ticket creation, updates, and history, including business logic for field
 * changes and user lookups.
 */
@Service
public class TicketMapper {

  private static final Logger logger = LoggerFactory.getLogger(TicketMapper.class);

  /** Repository for user entities, used for resolving assigned users. */
  private UserRepository userRepository;

  /**
   * Constructor for dependency injection.
   *
   * @param userRepository the user repository
   */
  public TicketMapper(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Maps a TicketCreationDto to a Ticket entity.
   *
   * @param ticketDto the ticket creation DTO
   * @return the Ticket entity
   */
  public Ticket map(TicketCreationDto ticketDto) {
    logger.info("Mapping TicketCreationDto to Ticket. Subject: {}", ticketDto.getSubject());
    // TODO add createdBy
    Ticket ticket =
        Ticket.builder()
            .subject(ticketDto.getSubject())
            .description(ticketDto.getDescription())
            .status(Status.NEW)
            .createdAt(Date.from(Instant.now()))
            .build();
    logger.debug("Mapped Ticket: {}", ticket);
    return ticket;
  }

  /**
   * Maps a Ticket entity to a TicketDto.
   *
   * @param ticket the Ticket entity
   * @return the TicketDto
   */
  public TicketDto map(Ticket ticket) {
    logger.info("Mapping Ticket to TicketDto. Ticket ID: {}", ticket.getId());
    TicketDtoBuilder ticketDtoBuilder =
        TicketDto.builder()
            .id(ticket.getId())
            .subject(ticket.getSubject())
            .description(ticket.getDescription())
            .status(ticket.getStatus())
            .createdAt(ticket.getCreatedAt())
            .modifiedAt(ticket.getModifiedAt());
    if (ticket.getAssignedTo() != null) {
      ticketDtoBuilder.assignedTo(ticket.getAssignedTo().getId().toString());
    }
    if (ticket.getCreatedBy() != null) {
      ticketDtoBuilder.createdBy(ticket.getCreatedBy().getId().toString());
    }
    if (ticket.getModifiedBy() != null) {
      ticketDtoBuilder.modifiedBy(ticket.getModifiedBy().getId().toString());
    }
    TicketDto dto = ticketDtoBuilder.build();
    logger.debug("Mapped TicketDto: {}", dto);
    return dto;
  }

  /**
   * Updates a Ticket entity based on a TicketUpdateDto and returns the list of TicketHistory
   * changes. Handles subject, description, assigned user, status, and comments.
   *
   * @param ticket the Ticket entity to update
   * @param ticketUpdateDto the update DTO
   * @return list of TicketHistory entries representing the changes
   * @throws ResponseStatusException if the assigned user is not found
   */
  public List<TicketHistory> update(Ticket ticket, TicketUpdateDto ticketUpdateDto) {
    logger.info("Updating Ticket entity with TicketUpdateDto. Ticket ID: {}", ticket.getId());
    Date updatedAt = Date.from(Instant.now());
    List<TicketHistory> ticketHistory = new ArrayList<>();

    if (StringUtils.isNotBlank(ticketUpdateDto.getSubject())
        && !ticketUpdateDto.getSubject().equals(ticket.getSubject())) {
      logger.debug(
          "Updating subject from '{}' to '{}'", ticket.getSubject(), ticketUpdateDto.getSubject());
      TicketHistory update =
          TicketHistory.builder()
              .type(ChangeType.SUBJECT)
              .text(ticket.getSubject() + " -> " + ticketUpdateDto.getSubject())
              .build();
      ticket.setSubject(ticketUpdateDto.getSubject());
      ticketHistory.add(update);
    }

    if (StringUtils.isNotBlank(ticketUpdateDto.getDescription())
        && !ticketUpdateDto.getDescription().equals(ticket.getDescription())) {
      logger.debug(
          "Updating description from '{}' to '{}'",
          ticket.getDescription(),
          ticketUpdateDto.getDescription());
      TicketHistory update =
          TicketHistory.builder()
              .type(ChangeType.DESCRIPTION)
              .text(ticket.getDescription() + " -> " + ticketUpdateDto.getDescription())
              .build();
      ticket.setDescription(ticketUpdateDto.getDescription());
      ticketHistory.add(update);
    }

    if (StringUtils.isNotBlank(ticketUpdateDto.getAssignedTo())) {
      logger.debug("Updating assignedTo to '{}'", ticketUpdateDto.getAssignedTo());
      Optional<User> optionalUser =
          userRepository.findById(UUID.fromString(ticketUpdateDto.getAssignedTo()));
      User user =
          optionalUser.orElseThrow(
              () -> {
                logger.warn("User not found for assignedTo: {}", ticketUpdateDto.getAssignedTo());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
              });
      if (ticket.getAssignedTo() == null
          || !ticketUpdateDto.getAssignedTo().equals(ticket.getAssignedTo().getId().toString())) {
        String assignedToId =
            (ticket.getAssignedTo() == null) ? "null" : ticket.getAssignedTo().getId().toString();
        TicketHistory update =
            TicketHistory.builder()
                .type(ChangeType.ASSIGNED_TO)
                .text(assignedToId + " -> " + ticketUpdateDto.getAssignedTo())
                .build();
        ticket.setAssignedTo(user);
        ticketHistory.add(update);
      }
    }

    if (ticketUpdateDto.getStatus() != null
        && !ticketUpdateDto.getStatus().equals(ticket.getStatus())) {
      logger.debug(
          "Updating status from '{}' to '{}'", ticket.getStatus(), ticketUpdateDto.getStatus());
      TicketHistory update =
          TicketHistory.builder()
              .type(ChangeType.STATUS)
              .text(ticket.getStatus() + " -> " + ticketUpdateDto.getStatus())
              .build();
      ticket.setStatus(ticketUpdateDto.getStatus());
      ticketHistory.add(update);
    }

    if (StringUtils.isNotBlank(ticketUpdateDto.getComment())) {
      logger.debug("Adding comment: {}", ticketUpdateDto.getComment());
      TicketHistory update =
          TicketHistory.builder()
              .type(ChangeType.COMMENT)
              .text(ticketUpdateDto.getComment())
              .build();
      ticketHistory.add(update);
    }

    // TODO add updated by
    ticketHistory.stream()
        .forEach(
            (h) -> {
              h.setUpdateDate(updatedAt);
            });
    ticket.setModifiedAt(updatedAt);
    logger.debug("Ticket history after update: {}", ticketHistory);
    return ticketHistory;
  }

  /**
   * Maps a TicketHistory entity to a TicketHistoryDto.
   *
   * @param ticketHistory the TicketHistory entity
   * @return the TicketHistoryDto
   */
  public TicketHistoryDto map(TicketHistory ticketHistory) {
    logger.info(
        "Mapping TicketHistory to TicketHistoryDto. TicketHistory ID: {}", ticketHistory.getId());
    // TODO add update by
    TicketHistoryDto dto =
        TicketHistoryDto.builder()
            // .updatedBy(ticketHistory.getUpdatedBy().getId().toString())
            .updateDate(ticketHistory.getUpdateDate())
            .type(ticketHistory.getType())
            .text(ticketHistory.getText())
            .build();
    logger.debug("Mapped TicketHistoryDto: {}", dto);
    return dto;
  }
}
