package biz.gelicon.core.convertors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Date;

public class DateDeserializer extends StdDeserializer<Date> {

    public DateDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if(jsonParser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        if(!jsonParser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            throw new RuntimeException(String.format("Неверный тип значения поля %s. Вместо \"%s\" ожидается число (Long)",
                    jsonParser.getCurrentName(),jsonParser.getValueAsString()));
        }
        return new Date(jsonParser.getValueAsLong());
    }
}
