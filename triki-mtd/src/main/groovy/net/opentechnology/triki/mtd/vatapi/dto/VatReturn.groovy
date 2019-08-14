package net.opentechnology.triki.mtd.vatapi.dto

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Builder
import lombok.Data

@Data
@Builder
class VatReturn {

    @JsonProperty
    String periodKey

    @JsonProperty
    BigDecimal vatDueSales

    @JsonProperty
    BigDecimal vatDueAcquisitions

    @JsonProperty
    BigDecimal totalVatDue

    @JsonProperty
    BigDecimal vatReclaimedCurrPeriod

    @JsonProperty
    BigDecimal netVatDue

    @JsonProperty
    Integer totalValueSalesExVAT

    @JsonProperty
    Integer totalValuePurchasesExVAT

    @JsonProperty
    Integer totalValueGoodsSuppliedExVAT

    @JsonProperty
    Integer totalAcquisitionsExVAT

    @JsonProperty
    boolean finalised
}
