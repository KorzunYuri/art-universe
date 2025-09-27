package yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

public record LastfmTagResponseDto(
    long id,
    String name,
    @Nullable String url,
    Integer approvalStatus,
    @Nullable Integer usageCount,
    @Nullable Integer usageUsersCount
) {

    public static LastfmTagResponseDto from(LastfmTag tag) {
        return new LastfmTagResponseDto(
            tag.getId(),
            tag.getName(),
            tag.getUrl(),
            tag.getApprovalStatus().getCode(),
            tag.getUsageCount(),
            tag.getUsageUsersCount()
        );
    }
}
