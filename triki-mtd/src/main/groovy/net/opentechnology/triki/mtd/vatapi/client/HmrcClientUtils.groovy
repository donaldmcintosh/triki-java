package net.opentechnology.triki.mtd.vatapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import net.opentechnology.triki.core.dto.SettingDto
import net.opentechnology.triki.mtd.module.HmrcVatModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory

import javax.inject.Inject;
import java.util.concurrent.TimeUnit

class HmrcClientUtils {

    private final ObjectMapper camelObjectMapper

    @Inject
    private SettingDto settingDto;

    public HmrcClientUtils(){
        camelObjectMapper =  new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

    private BearerAuthInterceptor getBearerAuthInterceptor(String token){
        return new BearerAuthInterceptor(token);
    }

    private OkHttpClient.Builder getHttpClientBuilder(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(logging);
    }

    private OkHttpClient getOkHttpClientWithBearerAuth(BearerAuthInterceptor bearerAuthInterceptor){
        return getHttpClientBuilder()
                .addNetworkInterceptor(bearerAuthInterceptor)
                .build();
    }

    private HmrcVatClient getHmrcVatClient(String baseUrl, ObjectMapper objectMapper, OkHttpClient okHttpClient){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        return retrofit.create(HmrcVatClient.class);
    }

    public HmrcVatClient buildAuthBearerServiceClient(String token) throws Exception {
        String hmrcUrlBase = settingDto.getSetting(HmrcVatModule.Settings.HMRCBASEURL.name());
        return getHmrcVatClient(hmrcUrlBase, camelObjectMapper, getOkHttpClientWithBearerAuth(getBearerAuthInterceptor(token)));
    }
}
