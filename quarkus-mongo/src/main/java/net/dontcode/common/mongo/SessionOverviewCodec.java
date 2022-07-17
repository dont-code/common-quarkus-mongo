package net.dontcode.common.mongo;

import com.mongodb.MongoClientSettings;
import net.dontcode.common.session.SessionOverview;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SessionOverviewCodec implements Codec<SessionOverview> {
    private final Codec<Document> documentCodec;

    public SessionOverviewCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public void encode(BsonWriter writer, SessionOverview session, EncoderContext encoderContext) {
        Document doc = new Document();
        doc.put("id", session.id());
        if( session.startTime() != null)
            doc.put("startTime", session.startTime().toInstant());
        if( session.endTime() != null)
            doc.put("endTime", session.endTime().toInstant());
        doc.put("eltCount", session.elementsCount());
        doc.put("demo", session.isDemo());
        documentCodec.encode(writer, doc, encoderContext);
    }


    @Override
    public Class<SessionOverview> getEncoderClass() {
        return SessionOverview.class;
    }

    @Override
    public SessionOverview decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        var changeDoc =document.get("change", Document.class);
        SessionOverview session = new SessionOverview(document.getString("_id"),
                ZonedDateTime.ofInstant(document.getDate("startTime").toInstant(), ZoneId.systemDefault()),
                ZonedDateTime.ofInstant(document.getDate("endTime").toInstant(), ZoneId.systemDefault()),
                document.getBoolean("isDemo")==true,
                document.getInteger("eltCount"));
        return session;
    }
}
