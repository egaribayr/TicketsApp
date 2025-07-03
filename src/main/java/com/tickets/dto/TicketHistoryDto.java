package com.tickets.dto;

import java.util.Date;

import com.tickets.model.ChangeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketHistoryDto {
    private ChangeType type;
    private Date updateDate;
    private String updatedBy;
    private String text;
}
