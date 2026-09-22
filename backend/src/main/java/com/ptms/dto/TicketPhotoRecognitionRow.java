package com.ptms.dto;

import java.time.LocalDate;

public record TicketPhotoRecognitionRow(
    String barcode,
    String physicalTicketNumber,
    Integer durationHours,
    Integer ticketNumber,
    LocalDate stockIssueDate,
    LocalDate expiryDate,
    Double confidence
) {}
