package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

import java.time.LocalDate

class VatObligation {

    enum VatObligationStatus {
        O("Open"),
        F("Fulfilled")

        private final description

        private VatObligationStatus(String description){
            this.description = description;
        }
    }

    LocalDate start
    LocalDate end
    LocalDate due
    VatObligationStatus status
    String periodKey
    LocalDate received

}
