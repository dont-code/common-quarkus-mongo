package net.dontcode.common.mongo;

import net.dontcode.common.session.SessionActionType;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class SessionActionTypeCodec implements Codec<SessionActionType> {
    @Override
    public SessionActionType decode(BsonReader bsonReader, DecoderContext decoderContext) {
        String val = bsonReader.readString();
        return SessionActionType.valueOf(val);
    }

    @Override
    public void encode(BsonWriter bsonWriter, SessionActionType sessionActionType, EncoderContext encoderContext) {
        bsonWriter.writeString(sessionActionType.name());
    }

    @Override
    public Class<SessionActionType> getEncoderClass() {
        return SessionActionType.class;
    }
}
