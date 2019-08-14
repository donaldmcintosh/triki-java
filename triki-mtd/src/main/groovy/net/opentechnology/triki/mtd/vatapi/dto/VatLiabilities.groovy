package net.opentechnology.triki.mtd.vatapi.dto

import lombok.Builder
import lombok.Data

@Data
@Builder
class VatLiabilities implements Serializable {

    List<VatLiability> liabilities
}
