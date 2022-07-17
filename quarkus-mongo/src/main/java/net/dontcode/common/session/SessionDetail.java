package net.dontcode.common.session;

import java.time.ZonedDateTime;

public record SessionDetail(String id, ZonedDateTime startTime,  ZonedDateTime endTime, boolean isDemo, int elementsCount, Object content) {
}
