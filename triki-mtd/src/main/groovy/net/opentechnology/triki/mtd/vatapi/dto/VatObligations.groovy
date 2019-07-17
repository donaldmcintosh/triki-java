package net.opentechnology.triki.mtd.vatapi.dto

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


public class VatObligations implements Serializable {

    @JsonProperty
    List<VatObligation> obligations = new ArrayList<>();

    @JsonProperty
    String code

    @JsonProperty
    String message

}
