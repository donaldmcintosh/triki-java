package net.opentechnology.triki.mtd.vatapi.client

import net.opentechnology.triki.mtd.vatapi.dto.VatLiabilities
import net.opentechnology.triki.mtd.vatapi.dto.VatObligations
import net.opentechnology.triki.mtd.vatapi.dto.VatPayments
import net.opentechnology.triki.mtd.vatapi.dto.VatReturn
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

public interface HmrcVatClient {

    @GET("organisations/vat/{vrn}/obligations")
    @Headers("Accept: application/vnd.hmrc.1.0+json")
    Call<VatObligations> getObligations(@Path("vrn") String vrn,
                                        @Query("from") String from,
                                        @Query("to") String to,
                                        @Query("status") String status,
                                        @HeaderMap Map<String, String> hmrcHeaders,
                                        @Header("Gov-Test-Scenario") String testScenario);

    @POST("organisations/vat/{vrn}/returns")
    @Headers("Accept: application/vnd.hmrc.1.0+json")
    Call<String> submitReturn(@Path("vrn") String vrn, @Body VatReturn vatReturn);

    @GET("organisations/vat/{vrn}/returns/{periodKey}")
    @Headers("Accept: application/vnd.hmrc.1.0+json")
    Call<VatReturn> viewReturn(@Path("vrn") String vrn, @Path("periodKey") String periodKey);

    @GET("organisations/vat/{vrn}/liabilities")
    @Headers("Accept: application/vnd.hmrc.1.0+json")
    Call<VatLiabilities> getLiabilities(@Path("vrn") String vrn);

    @GET("organisations/vat/{vrn}/payments")
    @Headers("Accept: application/vnd.hmrc.1.0+json")
    Call<VatPayments> getPayments(@Path("vrn") String vrn);
}
