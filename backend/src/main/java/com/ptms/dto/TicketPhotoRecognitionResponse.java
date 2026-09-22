package com.ptms.dto;

import java.util.List;

public record TicketPhotoRecognitionResponse(
    boolean succeeded,
    String source,
    String message,
    List<TicketPhotoRecognitionRow> tickets
) {}
