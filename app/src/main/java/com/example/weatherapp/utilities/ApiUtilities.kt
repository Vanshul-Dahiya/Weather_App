package com.example.weatherapp.utilities

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {

    private var retrofit : Retrofit? = null
    var BASE_URL = "https://api.openweathermap.org/data/2.5/"

//    call the function , whenever we need to call api
    fun getApiInterface():ApiInterface?{
        if (retrofit == null){
//            convert api correspond to model class
            retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit!!.create(ApiInterface::class.java)
    }
}