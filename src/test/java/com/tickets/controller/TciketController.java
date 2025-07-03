package com.tickets.controller;

import com.tickets.dto.TicketCreationDto;
import com.tickets.dto.TicketDto;
import com.tickets.dto.TicketHistoryDto;
import com.tickets.dto.TicketUpdateDto;
import com.tickets.model.ChangeType;
import com.tickets.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TciketController {
    @Mock
    private TicketService ticketService;
    @InjectMocks
    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketController = new TicketController(ticketService);
    }

    @Test
    void testCreateTicket() {
        TicketCreationDto creationDto = mock(TicketCreationDto.class);
        TicketDto ticketDto = mock(TicketDto.class);
        when(ticketService.createTicket(creationDto)).thenReturn(ticketDto);
        TicketDto result = ticketController.createTicket(creationDto);
        assertEquals(ticketDto, result);
        verify(ticketService).createTicket(creationDto);
    }

    @Test
    void testUpdateTicket() {
        String id = "123";
        TicketUpdateDto updateDto = mock(TicketUpdateDto.class);
        TicketDto ticketDto = mock(TicketDto.class);
        when(ticketService.updateTicket(eq(id), eq(updateDto))).thenReturn(ticketDto);
        TicketDto result = ticketController.updateTicket(id, updateDto);
        assertEquals(ticketDto, result);
        verify(ticketService).updateTicket(id, updateDto);
    }

    @Test
    void testGetTicketHistory() {
        String id = "123";
        ChangeType type = ChangeType.STATUS;
        List<TicketHistoryDto> historyList = Collections.singletonList(mock(TicketHistoryDto.class));
        when(ticketService.getTicketHistory(id, type)).thenReturn(historyList);
        List<TicketHistoryDto> result = ticketController.getTicketHistory(id, type);
        assertEquals(historyList, result);
        verify(ticketService).getTicketHistory(id, type);
    }

    @Test
    void testBulkImport() {
        MultipartFile file = new MockMultipartFile("file", "test.csv", MediaType.TEXT_PLAIN_VALUE, "data".getBytes());
        doNothing().when(ticketService).importTickets(file);
        ticketController.bulkImport(file);
        verify(ticketService).importTickets(file);
    }
}
