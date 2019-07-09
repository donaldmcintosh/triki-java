package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

@Data
@Builder
class VatReturn {
    String periodKey
    BigDecimal vatDueSales
    BigDecimal vatDueAcquisitions
    BigDecimal totalVatDue
    BigDecimal vatReclaimedCurrPeriod
    BigDecimal netVatDue
    Integer totalValueSalesExVAT
    Integer totalValuePurchasesExVAT
    Integer totalValueGoodsSuppliedExVAT
    Integer totalAcquisitionsExVAT
    boolean finalised
}
