package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

import java.time.LocalDate

@Data
@Builder
class VatLiability {

    TaxPeriod taxPeriod
    String type
    BigDecimal originalAmount
    BigDecimal outstandingAmount
    LocalDate due
}
