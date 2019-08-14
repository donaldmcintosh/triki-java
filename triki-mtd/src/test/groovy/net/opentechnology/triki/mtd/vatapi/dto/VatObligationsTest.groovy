package net.opentechnology.triki.mtd.vatapi.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import spock.lang.Specification

class VatObligationsTest extends Specification {

    def "check monthly none met"(String scenario){
        given:
        InputStream url = this.getClass().getClassLoader().getResourceAsStream("json/${scenario}.json")
        ObjectMapper camelObjectMapper =  new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);

        when:
        def jsonText = url.getText("UTF-8")
        VatObligations vatObligations = camelObjectMapper.readValue(jsonText, VatObligations)

        then:
        vatObligations

        where:
        scenario | _
        "MONTHLY_NONE_MET" | _
        "MONTHLY_ONE_MET" | _
        "MONTHLY_THREE_MET" | _
        "MONTHLY_TWO_MET" | _
        "NOT_FOUND" | _
        "QUARTERLY_FOUR_MET" | _
        "QUARTERLY_NONE_MET" | _
        "QUARTERLY_ONE_MET" | _
        "QUARTERLY_THREE_MET" | _
        "QUARTERLY_TWO_MET" | _
    }

    def "check date serialisation"(){
        given:
        InputStream url = this.getClass().getClassLoader().getResourceAsStream("json/MONTHLY_NONE_MET.json")
        ObjectMapper camelObjectMapper =  new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);

        when:
        def jsonText = url.getText("UTF-8")
        VatObligations vatObligations = camelObjectMapper.readValue(jsonText, VatObligations)

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        camelObjectMapper.writeValue(baos, vatObligations);

        then:
        baos.toString().contains("01 Jan 2017")

    }
}
