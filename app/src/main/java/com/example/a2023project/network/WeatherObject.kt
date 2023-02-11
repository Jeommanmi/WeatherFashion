package com.example.a2023project.network
import com.example.a2023project.ApiKey
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherObject {
    private fun getRetrofit(): Retrofit{
        return Retrofit.Builder()
            .baseUrl(ApiKey.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Json데이터를 사용자가 정의한 Java 객채로 변환해주는 라이브러리
            .build()
    }
    fun getRetrofitService(): WeatherInterface{
        return  getRetrofit().create(WeatherInterface::class.java) //retrofit객체 만듦!
    }


}