package net.opentechnology.triki.mtd.vatapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;

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
