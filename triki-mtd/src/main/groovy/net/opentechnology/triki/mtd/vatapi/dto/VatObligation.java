package net.opentechnology.triki.mtd.vatapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;
import net.opentechnology.triki.mtd.vatapi.serialisers.LocalDateSerializer;

public class VatObligation implements Serializable {

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty
    LocalDate start;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty
    LocalDate end;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty
    LocalDate due;

    @JsonProperty
    VatObligationStatus status;

    @JsonProperty
    String periodKey;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty
    LocalDate received;

}
