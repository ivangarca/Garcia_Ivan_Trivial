package com.example.garcia_ivan_trivial.model


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Definimos el modelo de lo que hay dentro de tu JSON
data class ApiInfo(
    val mensaje: String,
    val estado: String
)

// La interfaz con la ruta exacta que probamos antes
interface ApiService {

    @GET("ivangarca/Garcia_Ivan_Trivial/master/info.json")
    suspend fun getInfo(): ApiInfo

}

object RetrofitClient {

    private const val BASE_URL = "https://raw.githubusercontent.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}