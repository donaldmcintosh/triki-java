package net.opentechnology.triki.mtd.vatapi.dto;

import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


public class VatObligations {

    List<VatObligation> obligations = new ArrayList<>();
    String code
    String message

}
