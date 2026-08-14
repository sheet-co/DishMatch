package dev.sheet_co.dishMatch.dto;

import java.util.List;

// todo: switch options to a more appropriate dto for dish description
public record ChatResponse(String response, List<String> options) {}
