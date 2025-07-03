package com.tickets.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickets.dto.TicketCreationDto;
import com.tickets.dto.TicketDto;
import com.tickets.dto.TicketHistoryDto;
import com.tickets.dto.TicketUpdateDto;
import com.tickets.model.ChangeType;
import com.tickets.model.Status;
import com.tickets.model.Ticket;
import com.tickets.model.TicketHistory;
import com.tickets.model.User;
import com.tickets.repository.UserRepository;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

public class TicketMapperTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private TicketMapper ticketMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ticketMapper = new TicketMapper(userRepository);
  }

  @Test
  void testMapTicketCreationDtoToTicket() {
    TicketCreationDto dto = mock(TicketCreationDto.class);
    when(dto.getSubject()).thenReturn("subject");
    when(dto.getDescription()).thenReturn("desc");
    Ticket ticket = ticketMapper.map(dto);
    assertEquals("subject", ticket.getSubject());
    assertEquals("desc", ticket.getDescription());
    assertEquals(Status.NEW, ticket.getStatus());
    assertNotNull(ticket.getCreatedAt());
  }

  @Test
  void testMapTicketToTicketDto() {
    Ticket ticket =
        Ticket.builder()
            .id(UUID.randomUUID())
            .subject("subject")
            .description("desc")
            .status(Status.NEW)
            .createdAt(Date.from(Instant.now()))
            .modifiedAt(Date.from(Instant.now()))
            .build();
    TicketDto dto = ticketMapper.map(ticket);
    assertEquals(ticket.getSubject(), dto.getSubject());
    assertEquals(ticket.getDescription(), dto.getDescription());
    assertEquals(ticket.getStatus(), dto.getStatus());
    assertEquals(ticket.getCreatedAt(), dto.getCreatedAt());
    assertEquals(ticket.getModifiedAt(), dto.getModifiedAt());
  }

  @Test
  void testUpdateTicketWithSubjectAndDescription() {
    Ticket ticket = Ticket.builder().subject("old").description("oldDesc").build();
    TicketUpdateDto updateDto = mock(TicketUpdateDto.class);
    when(updateDto.getSubject()).thenReturn("new");
    when(updateDto.getDescription()).thenReturn("newDesc");
    when(updateDto.getAssignedTo()).thenReturn(null);
    when(updateDto.getStatus()).thenReturn(null);
    when(updateDto.getComment()).thenReturn(null);
    List<TicketHistory> history = ticketMapper.update(ticket, updateDto);
    assertEquals(2, history.size());
    assertEquals(ChangeType.SUBJECT, history.get(0).getType());
    assertEquals(ChangeType.DESCRIPTION, history.get(1).getType());
    assertEquals("old -> new", history.get(0).getText());
    assertEquals("oldDesc -> newDesc", history.get(1).getText());
  }

  @Test
  void testUpdateTicketWithAssignedTo() {
    Ticket ticket = Ticket.builder().subject("s").description("d").build();
    TicketUpdateDto updateDto = mock(TicketUpdateDto.class);
    String userId = UUID.randomUUID().toString();
    User user = User.builder().id(UUID.fromString(userId)).build();
    when(updateDto.getSubject()).thenReturn(null);
    when(updateDto.getDescription()).thenReturn(null);
    when(updateDto.getAssignedTo()).thenReturn(userId);
    when(updateDto.getStatus()).thenReturn(null);
    when(updateDto.getComment()).thenReturn(null);
    when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
    List<TicketHistory> history = ticketMapper.update(ticket, updateDto);
    assertEquals(1, history.size());
    assertEquals(ChangeType.ASSIGNED_TO, history.get(0).getType());
    assertEquals("null -> " + userId, history.get(0).getText());
    assertEquals(user, ticket.getAssignedTo());
  }

  @Test
  void testUpdateTicketWithAssignedTo_UserNotFound() {
    Ticket ticket = Ticket.builder().subject("s").description("d").build();
    TicketUpdateDto updateDto = mock(TicketUpdateDto.class);
    String userId = UUID.randomUUID().toString();
    when(updateDto.getSubject()).thenReturn(null);
    when(updateDto.getDescription()).thenReturn(null);
    when(updateDto.getAssignedTo()).thenReturn(userId);
    when(updateDto.getStatus()).thenReturn(null);
    when(updateDto.getComment()).thenReturn(null);
    when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> ticketMapper.update(ticket, updateDto));
  }

  @Test
  void testUpdateTicketWithStatusAndComment() {
    Ticket ticket = Ticket.builder().subject("s").description("d").status(Status.NEW).build();
    TicketUpdateDto updateDto = mock(TicketUpdateDto.class);
    when(updateDto.getSubject()).thenReturn(null);
    when(updateDto.getDescription()).thenReturn(null);
    when(updateDto.getAssignedTo()).thenReturn(null);
    when(updateDto.getStatus()).thenReturn(Status.CLOSED);
    when(updateDto.getComment()).thenReturn("A comment");
    List<TicketHistory> history = ticketMapper.update(ticket, updateDto);
    assertEquals(2, history.size());
    assertEquals(ChangeType.STATUS, history.get(0).getType());
    assertEquals(ChangeType.COMMENT, history.get(1).getType());
    assertEquals(Status.NEW + " -> " + Status.CLOSED, history.get(0).getText());
    assertEquals("A comment", history.get(1).getText());
    assertEquals(Status.CLOSED, ticket.getStatus());
  }

  @Test
  void testMapTicketHistoryToDto() {
    TicketHistory history =
        TicketHistory.builder()
            .updateDate(Date.from(Instant.now()))
            .type(ChangeType.COMMENT)
            .text("test comment")
            .build();
    TicketHistoryDto dto = ticketMapper.map(history);
    assertEquals(history.getUpdateDate(), dto.getUpdateDate());
    assertEquals(history.getType(), dto.getType());
    assertEquals(history.getText(), dto.getText());
  }
}
