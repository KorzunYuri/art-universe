package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmParserProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.BlacklistedEntityUrlService;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Service for validating DTO quality based on thresholds and blacklist status.
 * Supports two types of validation:
 * 1. For DTOs with metrics - checks thresholds + blacklist, can auto-blacklist low-quality entities
 * 2. For DTOs without metrics - checks blacklist only
 */
@Service
@Slf4j
public class DtoQualityService {

    private final ConfigPropertyHolder configPropertyHolder;
    private final BlacklistedEntityUrlService blacklistService;

    public DtoQualityService(
        ConfigPropertyHolder configPropertyHolder,
        BlacklistedEntityUrlService blacklistService
    ) {
        this.configPropertyHolder = configPropertyHolder;
        this.blacklistService = blacklistService;
    }

    // ========== Methods for DTOs WITH metrics ==========

    /**
     * Validates DTOs against thresholds only (no blacklist check)
     * Useful for checking quality before deciding whether to blacklist
     */
    public <E extends BaseLastfmEntity, M extends EntityDtoWithMetrics<E>> List<Result<E, M>> validateAgainstThreshold(List<M> dtos) {
        return dtos.stream()
            .map(this::validateAgainstThreshold)
            .toList();
    }

    /**
     * Validates DTOs against thresholds only (no blacklist check)
     * Useful for checking quality before deciding whether to blacklist
     */
    public <E extends BaseLastfmEntity, M extends EntityDtoWithMetrics<E>> Result<E, M> validateAgainstThreshold(M dto) {
        LastfmEntityType entityType = dto.getEntityType();
        Number metricValue = dto.getMetricValue();
        Integer threshold = getThreshold(entityType);

        if (metricValue != null && threshold != null) {
            if (metricValue.longValue() < threshold.longValue()) {
                return Result.rejected(dto, EntityQualityReason.BELOW_THRESHOLD,
                    String.format("Metric %d below threshold %s", metricValue.longValue(), threshold));
            }
        }

        return Result.accepted(dto);
    }

    /**
     * Validates DTO with metrics + automatically adds rejected to blacklist
     */
    public <E extends BaseLastfmEntity, M extends EntityDtoWithMetrics<E>> Result<E, M> validateAndBlacklist(M dto) {
        return validateAndBlacklist(List.of(dto)).getFirst();
    }

    /**
     * Validates DTOs with metrics + automatically adds rejected ones to blacklist
     * Only adds to blacklist if entity doesn't exist in DB and was rejected due to low metrics
     *
     * @return List of validation results for each DTO
     */
    public <E extends BaseLastfmEntity, M extends EntityDtoWithMetrics<E>> List<Result<E, M>> validateAndBlacklist(List<M> dtos) {
        if (dtos.isEmpty()) {
            return List.of();
        }

        LastfmEntityType entityType = dtos.getFirst().getEntityType();

        // First validate against thresholds only
        final boolean PASSED = true, FAILED = false;
        Map<Boolean, List<Result<E, M>>> thresholdResults = validateAgainstThreshold(dtos)
            .stream()
            .collect(groupingBy(Result::isAccepted));
        thresholdResults.putIfAbsent(PASSED, new ArrayList<>());
        thresholdResults.putIfAbsent(FAILED, new ArrayList<>());

        List<Result<E, M>> blacklistResults = validateAgainstBlacklist(thresholdResults.get(PASSED).stream().map(Result::getDto).toList());

        // Collect URLs to blacklist (failed threshold but not already blacklisted)
        List<String> blacklistUrlCandidates = thresholdResults.get(FAILED).stream()
            .map(Result::getDto)
            .map(EntityDto::getUrl)
            .toList();
        List<String> notBlacklistEligibleUrls = blacklistService.findAllNonBlackListable(entityType, blacklistUrlCandidates);
        List<String> urlsToBlacklist = blacklistUrlCandidates.stream()
            .filter(url -> !notBlacklistEligibleUrls.contains(url))
            .toList();

        // Batch add to blacklist
        if (!urlsToBlacklist.isEmpty()) {
            blacklistService.addToBlacklist(entityType, urlsToBlacklist);
            log.info("Added {} {} URLs to blacklist due to low quality metrics",
                urlsToBlacklist.size(), entityType);
        }

        // combine the result
        List<Result<E, M>> result = new ArrayList<>();
        result.addAll(thresholdResults.get(FAILED));
        result.addAll(blacklistResults);

        long acceptedCount = result.stream().mapToLong(r -> r.isAccepted() ? 1 : 0).sum();
        if (acceptedCount < dtos.size()) {
            log.debug("Validated {} {} DTOs: {} accepted, {} rejected, {} blacklisted",
                dtos.size(), entityType, acceptedCount,
                dtos.size() - acceptedCount, urlsToBlacklist.size());
        }

        return result;
    }

    /**
     * Validates list of DTOs without metrics - blacklist check only
     * Uses batch blacklist checking for performance
     */
    public <E extends BaseLastfmEntity, T extends EntityDto<E>> List<Result<E, T>> validateAgainstBlacklist(List<T> dtos) {
        if (dtos.isEmpty()) {
            return List.of();
        }

        LastfmEntityType entityType = dtos.getFirst().getEntityType();

        // Batch blacklist check
        List<String> urls = dtos.stream()
            .map(EntityDto::getUrl)
            .filter(Objects::nonNull)
            .filter(url -> !url.trim().isEmpty())
            .toList();

        Set<String> blacklistedUrls = new HashSet<>(
            blacklistService.getBlacklisted(entityType, urls)
        );

        List<Result<E, T>> results = dtos.stream()
            .map(dto -> blacklistedUrls.contains(dto.getUrl())
                ? Result.rejected(dto, EntityQualityReason.BLACKLISTED, "URL is blacklisted: " + dto.getUrl())
                : Result.accepted(dto)
            )
            .toList();

        long rejectedCount = results.stream().mapToLong(r -> r.isRejected() ? 1 : 0).sum();
        if (rejectedCount > 0) {
            log.debug("Validated {} {} DTOs against blacklist: {} rejected",
                dtos.size(), entityType, rejectedCount);
        }

        return results;
    }

    private Integer getThreshold(LastfmEntityType entityType) {
        return switch (entityType) {
            case ARTIST -> configPropertyHolder.getInt(LastfmParserProperty.THRESHOLD_ARTIST_LISTENERS_COUNT);
            case ALBUM -> configPropertyHolder.getInt(LastfmParserProperty.THRESHOLD_ALBUM_PLAY_COUNT);
            case TRACK -> configPropertyHolder.getInt(LastfmParserProperty.THRESHOLD_TRACK_PLAY_COUNT);
            case TAG -> configPropertyHolder.getInt(LastfmParserProperty.THRESHOLD_TAG_USAGE_COUNT);
        };
    }

    // ========== Result classes ==========

    /**
     * Result of entity quality validation
     */
    @Getter
    public static class Result<E extends BaseLastfmEntity, T extends EntityDto<E>> {
        private final T dto;
        private final boolean accepted;
        private final EntityQualityReason reason;
        private final String message;

        private Result(T dto, boolean accepted, EntityQualityReason reason, String message) {
            this.dto = dto;
            this.accepted = accepted;
            this.reason = reason;
            this.message = message;
        }

        public static <E extends BaseLastfmEntity, T extends EntityDto<E>> Result<E, T> accepted(T dto) {
            return new Result<>(dto, true, EntityQualityReason.ACCEPTED, null);
        }

        public static <E extends BaseLastfmEntity, T extends EntityDto<E>> Result<E, T> rejected(
            T dto,
            EntityQualityReason reason,
            String message
        ) {
            return new Result<>(dto, false, reason, message);
        }

        public boolean isRejected() {
            return !accepted;
        }

        @Override
        public String toString() {
            return String.format("EntityQualityResult{accepted=%s, reason=%s, message='%s'}",
                accepted, reason, message);
        }
    }

    /**
     * Reasons why an entity might be rejected
     */
    public enum EntityQualityReason {
        ACCEPTED,
        BLACKLISTED,
        BELOW_THRESHOLD
    }
}
