package net.opentechnology.triki.mtd.vatapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;

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
