package com.tickets.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tickets.dto.TicketUpdateDto;
import com.tickets.dto.TicketCreationDto;
import com.tickets.dto.TicketDto;
import com.tickets.dto.TicketHistoryDto;
import com.tickets.model.ChangeType;
import com.tickets.service.TicketService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for managing ticket operations.
 * <p>
 * Provides endpoints for creating, updating, retrieving ticket history, and bulk importing tickets.
 * </p>
 * <ul>
 *   <li><b>POST /api/tickets</b>: Create a new ticket</li>
 *   <li><b>PUT /api/tickets/{id}</b>: Update an existing ticket</li>
 *   <li><b>GET /api/tickets/{id}/history</b>: Retrieve ticket history, optionally filtered by change type</li>
 *   <li><b>POST /api/tickets/bulkimport</b>: Bulk import tickets from a CSV file</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    /**
     * Service for ticket business logic.
     */
    private TicketService ticketService;

    /**
     * Constructor for dependency injection.
     * @param ticketService the ticket service
     */
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Creates a new ticket.
     * @param ticketCreationDto the ticket creation data
     * @return the created ticket DTO
     */
    @PostMapping()
    public TicketDto createTicket(@RequestBody TicketCreationDto ticketCreationDto) {
        logger.info("Received request to create ticket with subject: {}", ticketCreationDto.getSubject());
        TicketDto result = ticketService.createTicket(ticketCreationDto);
        logger.debug("Created ticket: {}", result);
        return result;
    }

    /**
     * Updates an existing ticket by ID.
     * @param id the ticket ID
     * @param tickerUpdateDto the ticket update data
     * @return the updated ticket DTO
     */
    @PutMapping("/{id}")
    public TicketDto updateTicket(@PathVariable String id, @RequestBody TicketUpdateDto tickerUpdateDto) {
        logger.info("Received request to update ticket with id: {}", id);
        TicketDto result = ticketService.updateTicket(id, tickerUpdateDto);
        logger.debug("Updated ticket: {}", result);
        return result;
    }

    /**
     * Retrieves the history of a ticket, optionally filtered by change type.
     * @param id the ticket ID
     * @param type (optional) the change type to filter by
     * @return list of ticket history DTOs
     */
    @GetMapping("/{id}/history")
    public List<TicketHistoryDto> getTicketHistory(@PathVariable String id,
            @RequestParam(required = false) ChangeType type) {
        logger.info("Received request to get history for ticket id: {} with type: {}", id, type);
        List<TicketHistoryDto> result = ticketService.getTicketHistory(id, type);
        logger.debug("Ticket history result: {}", result);
        return result;
    }

    /**
     * Bulk imports tickets from a CSV file.
     * @param file the CSV file containing tickets
     */
    @PostMapping( value = "/bulkimport", 
               consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public void bulkImport(@RequestPart(required = true) MultipartFile file) {
        logger.info("Received request to bulk import tickets from file: {}", file.getOriginalFilename());
        ticketService.importTickets(file);
        logger.info("Bulk import completed for file: {}", file.getOriginalFilename());
    }

}
