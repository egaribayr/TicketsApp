package com.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public class TicketServiceTest {
  @Mock private TicketRepository ticketRepository;
  @Mock private TicketHistoryRepository ticketHistoryRepository;
  @Mock private TicketMapper ticketMapper;
  @InjectMocks private TicketService ticketService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ticketService = new TicketService(ticketRepository, ticketHistoryRepository, ticketMapper);
  }

  @Test
  void testCreateTicket() {
    TicketCreationDto creationDto = new TicketCreationDto();
    Ticket ticket = Ticket.builder().build();
    Ticket savedTicket = Ticket.builder().build();
    TicketDto ticketDto = new TicketDto();

    when(ticketMapper.map(creationDto)).thenReturn(ticket);
    when(ticketRepository.saveAndFlush(ticket)).thenReturn(savedTicket);
    when(ticketMapper.map(savedTicket)).thenReturn(ticketDto);

    TicketDto result = ticketService.createTicket(creationDto);
    assertEquals(ticketDto, result);
    verify(ticketRepository).saveAndFlush(ticket);
  }

  @Test
  void testUpdateTicket_Success() {
    String id = UUID.randomUUID().toString();
    TicketUpdateDto updateDto = new TicketUpdateDto();
    Ticket ticket = Ticket.builder().ticketHistory(new ArrayList<>()).build();
    List<TicketHistory> historyList = Collections.singletonList(new TicketHistory());
    TicketDto ticketDto = new TicketDto();

    when(ticketRepository.findById(UUID.fromString(id))).thenReturn(Optional.of(ticket));
    when(ticketMapper.update(ticket, updateDto)).thenReturn(historyList);
    when(ticketHistoryRepository.saveAllAndFlush(historyList)).thenReturn(historyList);
    when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);
    when(ticketMapper.map(ticket)).thenReturn(ticketDto);

    TicketDto result = ticketService.updateTicket(id, updateDto);
    assertEquals(ticketDto, result);
    verify(ticketRepository).findById(UUID.fromString(id));
    verify(ticketHistoryRepository).saveAllAndFlush(historyList);
    verify(ticketRepository).saveAndFlush(ticket);
  }

  @Test
  void testUpdateTicket_NotFound() {
    String id = UUID.randomUUID().toString();
    TicketUpdateDto updateDto = new TicketUpdateDto();
    when(ticketRepository.findById(UUID.fromString(id))).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> ticketService.updateTicket(id, updateDto));
  }

  @Test
  void testGetTicketHistory_AllTypes() {
    String id = UUID.randomUUID().toString();
    Ticket ticket = Ticket.builder().ticketHistory(new ArrayList<>()).build();
    TicketHistory history = new TicketHistory();
    ticket.getTicketHistory().add(history);
    when(ticketRepository.findById(UUID.fromString(id))).thenReturn(Optional.of(ticket));
    when(ticketMapper.map(history)).thenReturn(new TicketHistoryDto());
    List<TicketHistoryDto> result = ticketService.getTicketHistory(id, null);
    assertEquals(1, result.size());
  }

  @Test
  void testGetTicketHistory_FilteredType() {
    String id = UUID.randomUUID().toString();
    ChangeType type = ChangeType.STATUS;
    TicketHistory history = new TicketHistory();
    history.setType(type);
    Ticket ticket = Ticket.builder().ticketHistory(new ArrayList<>()).build();
    ticket.getTicketHistory().add(history);
    when(ticketRepository.findById(UUID.fromString(id))).thenReturn(Optional.of(ticket));
    when(ticketMapper.map(history)).thenReturn(new TicketHistoryDto());
    List<TicketHistoryDto> result = ticketService.getTicketHistory(id, type);
    assertEquals(1, result.size());
  }

  @Test
  void testGetTicketHistory_NotFound() {
    String id = UUID.randomUUID().toString();
    when(ticketRepository.findById(UUID.fromString(id))).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> ticketService.getTicketHistory(id, null));
  }

  @Test
  void testImportTickets() throws IOException {
    String csv = "subject,description,NEW\nsubject2,description2,CLOSED";
    MultipartFile file = mock(MultipartFile.class);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
    ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
    when(ticketRepository.saveAllAndFlush(anyList())).thenReturn(Collections.emptyList());
    ticketService.importTickets(file);
    verify(ticketRepository).saveAllAndFlush(captor.capture());
    List<Ticket> tickets = captor.getValue();
    assertEquals(2, tickets.size());
    assertEquals("subject", tickets.get(0).getSubject());
    assertEquals("description2", tickets.get(1).getDescription());
    assertEquals(Status.NEW, tickets.get(0).getStatus());
    assertEquals(Status.CLOSED, tickets.get(1).getStatus());
  }
}
