package net.opentechnology.triki.mtd.vatapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

public class VatError implements Serializable {

    @JsonProperty
    Integer statusCode

    @JsonProperty
    String code

    @JsonProperty
    String message

}
