package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

import java.time.LocalDate

@Data
@Builder
class VatLiability implements Serializable{
    TaxPeriod taxPeriod
    String type
    BigDecimal originalAmount
    BigDecimal outstandingAmount
    LocalDate due
}
