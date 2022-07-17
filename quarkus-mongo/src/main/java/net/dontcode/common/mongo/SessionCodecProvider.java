package net.dontcode.common.mongo;

import net.dontcode.common.session.Session;
import net.dontcode.common.session.SessionOverview;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import net.dontcode.common.session.SessionActionType;

public class SessionCodecProvider implements CodecProvider {
        @Override
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            if (clazz == Session.class) {
                return (Codec<T>) new SessionCodec();
            } else if (clazz == SessionActionType.class) {
                return (Codec<T>) new SessionActionTypeCodec();
            } else if (clazz == SessionOverview.class)  {
                return (Codec<T>) new SessionOverviewCodec();
            }
            return null;
        }

    }
