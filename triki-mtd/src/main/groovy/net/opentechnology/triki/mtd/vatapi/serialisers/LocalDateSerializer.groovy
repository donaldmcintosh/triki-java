package net.opentechnology.triki.mtd.vatapi.serialisers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateSerializer extends JsonSerializer<LocalDate> {

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yy")

    @Override
    void serialize(LocalDate localDate, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(localDate.format(dtf));
    }
}
