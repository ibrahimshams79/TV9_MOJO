package com.tv9.tv9MoJo.networking;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;


public interface ApiConfig {

    @Multipart
    @POST("upload_images.php")
    Call<ServerResponse> upload(
            @Header("Authorization") String authorization,
            @PartMap Map<String, RequestBody> map,
            @Part("stringdescription") String stringdescription
    );

    @Multipart
    @POST("upload_pdf.php")
    Call<ServerResponse> uploadpdf(
            @Header("Authorization") String authorization,
            @PartMap Map<String, RequestBody> map,
            @Part("stringdescription") String stringdescription
    );

    @Multipart
    @POST("submit_story.php")
    Call<ServerResponse> uploadTextStory(
            @Header("Authorization") String authorization,
            @Part("storyWithText") String story,
            @Part("description") String desc
    );

}