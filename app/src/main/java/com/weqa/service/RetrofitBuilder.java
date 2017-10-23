package com.weqa.service;

import com.weqa.framework.ErrorHandlingAdapter;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pc on 8/4/2017.
 */

public class RetrofitBuilder {

    public static Retrofit getRetrofit() {

        //HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://10.0.2.2:60064/")
                //.baseUrl("http://weqaapplication.azurewebsites.net/")
                //.baseUrl("http://weqaappdev.azurewebsites.net/")
                .baseUrl("http://weqaapp1010.azurewebsites.net/")
                .addCallAdapterFactory(new ErrorHandlingAdapter.ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                //.client(client)
                .build();

        return retrofit;
    }
}
