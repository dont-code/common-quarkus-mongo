package net.dontcode.common.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dontcode.core.Message;

import jakarta.websocket.*;

public class MessageEncoderDecoder implements Decoder.Text<Message>, Encoder.Text<Message> {
    @Override
    public Message decode(String text) throws DecodeException {
        ObjectMapper mapper = new ObjectMapper();
        Message obj = null;
        try {
            obj = mapper.readValue(text, Message.class);
        } catch (JsonProcessingException e) {
            throw new DecodeException(text, "Cannot decode Message", e);
        }
        return obj;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String encode(Message message) throws EncodeException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new EncodeException(message, "Error encoding message", e);
        }
        return json;
    }
}
