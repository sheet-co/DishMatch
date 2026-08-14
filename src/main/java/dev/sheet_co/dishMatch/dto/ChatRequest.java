package dev.sheet_co.dishMatch.dto;

public record ChatRequest(String message, long userId, long chatId, String userName) {}
