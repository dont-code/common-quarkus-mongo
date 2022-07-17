package net.dontcode.common.session;

import java.time.ZonedDateTime;

public record SessionOverview(String id, ZonedDateTime startTime,  ZonedDateTime endTime, boolean isDemo, int elementsCount) {

}
