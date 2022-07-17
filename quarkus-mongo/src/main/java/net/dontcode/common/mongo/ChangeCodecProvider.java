package net.dontcode.common.mongo;

import net.dontcode.core.Change;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ChangeCodecProvider  implements CodecProvider {
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == Change.class) {
            return (Codec<T>) new ChangeCodec();
        }
        return null;
    }

}
