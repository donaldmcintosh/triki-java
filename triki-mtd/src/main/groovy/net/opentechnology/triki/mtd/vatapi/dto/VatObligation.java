package net.opentechnology.triki.mtd.vatapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;
import net.opentechnology.triki.mtd.vatapi.serialisers.LocalDateSerializer;

@Getter
public class VatObligation implements Serializable {

    @JsonProperty
    LocalDate start;

    @JsonProperty
    LocalDate end;

    @JsonProperty
    LocalDate due;

    @JsonProperty
    VatObligationStatus status;

    @JsonProperty
    String periodKey;

    @JsonProperty
    LocalDate received;

}
