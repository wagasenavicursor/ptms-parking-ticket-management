package com.ptms.dto;

import java.util.List;

public record TicketPhotoRecognitionResponse(
    boolean azureConfigured,
    boolean succeeded,
    String source,
    String message,
    List<TicketPhotoRecognitionRow> tickets
) {}
