package com.tickets.service;

import com.tickets.dto.TicketCreationDto;
import com.tickets.dto.TicketDto;
import com.tickets.dto.TicketHistoryDto;
import com.tickets.dto.TicketUpdateDto;
import com.tickets.model.ChangeType;
import com.tickets.model.Status;
import com.tickets.model.Ticket;
import com.tickets.model.TicketHistory;
import com.tickets.repository.TicketHistoryRepository;
import com.tickets.repository.TicketRepository;
import com.tickets.util.TicketMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class for handling business logic related to tickets.
 *
 * <p>Provides methods for creating, updating, retrieving history, and importing tickets.
 */
@Service
public class TicketService {

  private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

  /** Repository for ticket entities. */
  private final TicketRepository ticketRepository;

  /** Repository for ticket history entities. */
  private final TicketHistoryRepository ticketHistoryRepository;

  /** Mapper for converting between DTOs and entities. */
  private final TicketMapper ticketMapper;

  /**
   * Constructor for dependency injection.
   *
   * @param ticketRepository the ticket repository
   * @param ticketHistoryRepository the ticket history repository
   * @param ticketMapper the ticket mapper
   */
  public TicketService(
      TicketRepository ticketRepository,
      TicketHistoryRepository ticketHistoryRepository,
      TicketMapper ticketMapper) {
    this.ticketRepository = ticketRepository;
    this.ticketHistoryRepository = ticketHistoryRepository;
    this.ticketMapper = ticketMapper;
  }

  /**
   * Retrieves a list of tickets.
   *
   * @param id (optional) the assigned user ID to filter by; if blank, returns all tickets
   * @return list of ticket DTOs
   */
  public List<TicketDto> getTikets(String id) {

    List<Ticket> tickets;
    if (StringUtils.isBlank(id)) {
      tickets = ticketRepository.findAll();
    } else {
      tickets = ticketRepository.findAllByAssignedTo_Id(UUID.fromString(id));
    }

    return tickets.stream().map(ticketMapper::map).collect(Collectors.toList());
  }

  /**
   * Creates a new ticket.
   *
   * @param ticketDto the ticket creation DTO
   * @return the created ticket as a DTO
   */
  public TicketDto createTicket(TicketCreationDto ticketDto) {
    logger.info("Creating new ticket with subject: {}", ticketDto.getSubject());
    Ticket ticket = ticketMapper.map(ticketDto);
    TicketDto result = ticketMapper.map(ticketRepository.saveAndFlush(ticket));
    logger.debug("Created ticket: {}", result);
    return result;
  }

  /**
   * Updates an existing ticket by ID.
   *
   * @param id the ticket ID
   * @param tickerUpdateDto the ticket update DTO
   * @return the updated ticket as a DTO
   * @throws ResponseStatusException if the ticket is not found
   */
  public TicketDto updateTicket(String id, TicketUpdateDto tickerUpdateDto) {
    logger.info("Updating ticket with id: {}", id);
    Optional<Ticket> optionalTicket = ticketRepository.findById(UUID.fromString(id));
    Ticket ticket =
        optionalTicket.orElseThrow(
            () -> {
              logger.warn("Ticket not found for id: {}", id);
              return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");
            });
    List<TicketHistory> ticketHistory = ticketMapper.update(ticket, tickerUpdateDto);
    logger.debug("Ticket history updates: {}", ticketHistory);
    ticket.getTicketHistory().addAll(ticketHistoryRepository.saveAllAndFlush(ticketHistory));
    TicketDto result = ticketMapper.map(ticketRepository.saveAndFlush(ticket));
    logger.debug("Updated ticket: {}", result);
    return result;
  }

  /**
   * Retrieves the history of a ticket, optionally filtered by change type.
   *
   * @param id the ticket ID
   * @param type (optional) the change type to filter by
   * @return list of ticket history DTOs
   * @throws ResponseStatusException if the ticket is not found
   */
  public List<TicketHistoryDto> getTicketHistory(String id, ChangeType type) {
    logger.info("Retrieving history for ticket id: {} with change type: {}", id, type);
    Optional<Ticket> optionalTicket = ticketRepository.findById(UUID.fromString(id));
    Ticket ticket =
        optionalTicket.orElseThrow(
            () -> {
              logger.warn("Ticket not found for id: {}", id);
              return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");
            });
    List<TicketHistoryDto> result;
    if (type == null) {
      result =
          ticket.getTicketHistory().stream().map(ticketMapper::map).collect(Collectors.toList());
    } else {
      result =
          ticket.getTicketHistory().stream()
              .filter(th -> th.getType().equals(type))
              .map(ticketMapper::map)
              .collect(Collectors.toList());
    }
    logger.debug("Ticket history result: {}", result);
    return result;
  }

  /**
   * Imports tickets from a CSV file.
   *
   * @param file the CSV file containing tickets
   */
  public void importTickets(MultipartFile file) {
    logger.info("Importing tickets from file: {}", file.getOriginalFilename());
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
      Date createdAt = Date.from(Instant.now());
      List<Ticket> importedTickets = new ArrayList<>();
      while (reader.ready()) {
        String line = reader.readLine();
        String[] tokens = line.split(",");
        Ticket ticket =
            Ticket.builder()
                .subject(tokens[0])
                .description(tokens[1])
                .status(Status.valueOf(tokens[2]))
                .createdAt(createdAt)
                .build();
        importedTickets.add(ticket);
        logger.debug("Parsed ticket from CSV: {}", ticket);
      }
      ticketRepository.saveAllAndFlush(importedTickets);
      logger.info("Successfully imported {} tickets", importedTickets.size());
    } catch (IOException e) {
      logger.error("Error importing tickets from file: {}", file.getOriginalFilename(), e);
      e.printStackTrace();
    }
  }
}
