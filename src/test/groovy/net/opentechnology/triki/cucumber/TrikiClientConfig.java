package net.opentechnology.triki.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration

@ComponentScan(basePackages = "net.opentechnology.triki.cucumber")
public class TrikiClientConfig {

//    @Value('${private.url}')
    private String trikiUrl = "http://localhost:8080";

    @Bean
    public ObjectMapper getObjectMapper(){
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

    private OkHttpClient.Builder getHttpClientBuilder(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(logging);
    }

    private OkHttpClient getOkHttpClient(){
        return getHttpClientBuilder()
                .build();
    }

    private RetrofitTrikiClient getTrikiClient(String baseUrl, ObjectMapper objectMapper, OkHttpClient okHttpClient){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        return retrofit.create(RetrofitTrikiClient.class);
    }

    @Bean
    public RetrofitTrikiClient buildTrikiClient(ObjectMapper objectMapper) throws Exception {
        return getTrikiClient(trikiUrl, objectMapper, getOkHttpClient());
    }
}
