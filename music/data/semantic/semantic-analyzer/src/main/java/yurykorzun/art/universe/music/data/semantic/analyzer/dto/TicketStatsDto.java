package yurykorzun.art.universe.music.data.semantic.analyzer.dto;

public record TicketStatsDto(
    long pending,
    long processing,
    long completed,
    long failed
) {}
