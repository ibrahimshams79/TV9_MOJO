package com.tv9.tv9MoJo.networking;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AppConfig {

   //public static String BASE_URL = "http://192.168.43.68/tv9/";
//   public static String BASE_URL = "http://192.168.3.81/tv9/";
    public static String BASE_URL = "http://192.168.0.103/tv9/";

//   public static String BASE_URL = "https://files.000webhost.com/TV9";

    public static Retrofit getRetrofit(String url) {

//        Gson gson = new GsonBuilder()
//                .setLenient().create();

        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
//        OkHttpClient httpClient;
//        httpClient = new OkHttpClient();
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        httpClient.interceptors().add(interceptor);
//        Retrofit retrofit = createAdapter().build();
//        service = retrofit.create(IService.class);
    }
}
