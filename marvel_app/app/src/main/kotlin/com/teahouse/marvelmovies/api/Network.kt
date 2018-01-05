package com.teahouse.marvelmovies

import rx.Observable

import retrofit.http.*
import retrofit.Retrofit
import retrofit.GsonConverterFactory
import retrofit.RxJavaCallAdapterFactory

import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.logging.HttpLoggingInterceptor

interface MarvelService {

    @Headers("Accept: */*")
    @GET("/v1/public/characters")
    public fun getCharacters(@Query("ts") ts: String,
                             @Query("apikey") apiKey: String,
                             @Query("hash") hash: String,
                             @Query("limit") limit: Int)
            : Observable<Model.CharacterResponse>

    @Headers("Accept: */*")
    @GET("/v1/public/characters/{id}")
    public fun getCharacterDetail(@Path("id") id: String,
                                  @Query("ts") ts: String,
                                  @Query("apikey") apiKey: String,
                                  @Query("hash") hash: String)
            : Observable<Model.CharacterResponse>

    @Headers("Accept: */*")
    @GET("/v1/public/{type}/{id}")
    public fun getDetail(@Path("type") type: String,
                         @Path("id") id: String,
                         @Query("ts") ts: String,
                         @Query("apikey") apiKey: String,
                         @Query("hash") hash: String)
            : Observable<Model.DetailResponse>

    companion object {
        val BASE_URL = "http://gateway.marvel.com"
        val API_URL = "/v1/public/"

        fun create() : MarvelService {
            val gsonBuilder = GsonBuilder()

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client: OkHttpClient = OkHttpClient()
            client.interceptors().add(loggingInterceptor)

            val restAdapter = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .client(client)
                    .build()

            return restAdapter.create(MarvelService::class.java)
        }
    }

}
