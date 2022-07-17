package net.dontcode.common.session;

import net.dontcode.core.Change;

import java.time.ZonedDateTime;

public record Session (String id, ZonedDateTime time, SessionActionType type, String srcInfo, Change change) {

}
