package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

import java.time.LocalDate

@Data
@Builder
class VatPayment {
    BigDecimal amount
    LocalDate received
}
