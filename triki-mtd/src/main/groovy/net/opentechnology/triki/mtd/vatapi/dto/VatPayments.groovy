package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

@Data
@Builder
class VatPayments {
    List<VatPayments> payments
}
