package com.example.weatherapp.utilities

import com.example.weatherapp.POJO.ModelClass
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

//    https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
//    lat , lon , APPID are called of this api

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") latitude : String,
        @Query("lon") longitude : String,
        @Query("APPID") api_key : String
    ) : Call<ModelClass>

//    https://api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") city_name : String,
        @Query("APPID") api_key : String
    ) : Call<ModelClass>
}