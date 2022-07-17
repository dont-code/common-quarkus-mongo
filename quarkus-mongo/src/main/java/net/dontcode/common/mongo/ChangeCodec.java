package net.dontcode.common.mongo;

import com.mongodb.MongoClientSettings;
import net.dontcode.core.Change;
import net.dontcode.core.DontCodeModelPointer;
import org.bson.BSONObject;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.HashMap;
import java.util.Map;

public class ChangeCodec implements Codec<Change> {

    private final Codec<Document> documentCodec;

    public ChangeCodec () {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public Change decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return fromDocument(document);
    }

    public Change fromDocument (Document document) {
        if( document==null)
            return null;

        Change chg = new Change();
        if (document.getString("position") != null) {
            chg.setPosition(document.getString("position"));
        }
        if (document.getString("type") != null) {
            chg.setType(Change.ChangeType.valueOf(document.getString("type")));
        }
        if (document.get("pointer") != null) {
            Document ptr = (Document) document.get("pointer");
            DontCodeModelPointer pointer = new DontCodeModelPointer();
            pointer.setPosition((String) ptr.get("position"));
            pointer.setSchemaPosition((String) ptr.get("schemaPosition"));
            pointer.setContainerPosition((String) ptr.get("containerPosition"));
            pointer.setContainerSchemaPosition((String) ptr.get("containerSchemaPosition"));
            pointer.setItemId((String) ptr.get("itemId"));
            pointer.setKey((String) ptr.get("key"));
            chg.setPointer(pointer);
        }

        var value = document.get("value");
        if( value instanceof String) {
            chg.setValue(value);
        } else if (value instanceof Map) {
            chg.setValue(new HashMap((Map)value));  // We don't set directly the BSon map as the implementation may provide some side effects
        }
        return chg;

    }

    public Document toDocument (Change chg) {
        Document doc = new Document();
        if( chg.getPosition() != null)
            doc.put("position", chg.getPosition());
        if( chg.getType() != null)
            doc.put("type", chg.getType().name());
        if( chg.getPointer()!= null) {
            DontCodeModelPointer pointer = chg.getPointer();
            Document ptr = new Document();
            ptr.put("position", pointer.getPosition());
            ptr.put("schemaPosition", pointer.getSchemaPosition());
            ptr.put("containerPosition", pointer.getContainerPosition());
            ptr.put("schemaContainerPosition", pointer.getContainerSchemaPosition());
            if( pointer.getItemId()!=null) {
                ptr.put("itemId", pointer.getItemId());
            }
            if( pointer.getKey()!=null) {
                ptr.put("key", pointer.getKey());
            }
            doc.put("pointer", ptr);
        }

        if( (chg.getValue()!=null)) {
            if( chg.getValue() instanceof String) {
                doc.put("value", chg.getValue());
            } else if (chg.getValue() instanceof Map) {
                Document value = new Document((Map) chg.getValue());
                doc.put("value", value);
            } else {
                throw new RuntimeException("Cannot encode in Bson the Change value of type "+chg.getValue().getClass().getName());
            }
        }
        return doc;
    }


    @Override
    public void encode(BsonWriter writer, Change chg, EncoderContext encoderContext) {
        Document doc = toDocument(chg);
        documentCodec.encode(writer, doc, encoderContext);
    }

    @Override
    public Class<Change> getEncoderClass() {
        return Change.class;
    }
}
