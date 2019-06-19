package net.opentechnology.triki.cucumber;

import retrofit2.Call;
import retrofit2.http.*;

import javax.ws.rs.Produces;

public interface RetrofitTrikiClient {

    @POST("/ui/")
    @Produces("text/html")
    @FormUrlEncoded
    Call<String> submitLogin(@Field("username") String login,
                             @Field("password") String password);

    @GET("/auth/logoff")
    Call<String> logoff(@Header("Cookie") String sessionId);


}