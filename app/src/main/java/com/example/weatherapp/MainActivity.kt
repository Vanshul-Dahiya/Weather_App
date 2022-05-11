package com.example.weatherapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.POJO.ModelClass
import com.example.weatherapp.utilities.ApiUtilities
import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        rl_mainLayout.visibility = View.GONE

        getCurrentLocation()


        et_get_city_name.setOnEditorActionListener{ v , actionId , keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                getCityWeather(et_get_city_name.text.toString())
                val view = this.currentFocus
                if (view!=null){
                    val imm : InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                    et_get_city_name.clearFocus()
                }
                true
            }else false
        }

    }

    private fun getCityWeather(cityName: String) {
        pb_loading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, API_KEY)?.enqueue(object :
        Callback<ModelClass>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                setDataOnViews(response.body())
            }

            override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Enter a valid city name", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun getCurrentLocation() {
//        check permission
        if (checkPermissions()) {
//            location also enabled then we get lat & long
            if (isLocationEnabled()) {
//                get lat and long here

                if (ActivityCompat.checkSelfPermission(this,
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location = task.result
                    if (location == null) {
                        Toast.makeText(this, "Exceptional & rare error", Toast.LENGTH_SHORT).show()
                    } else {
                        fetchCurrentLocationWeather(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    }


                }
            } else {
//                open settings
                Toast.makeText(this, "Please ,turn on the location !!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

            }
        } else {
//            request permission
            requestPermission()
        }

    }

    private fun fetchCurrentLocationWeather(lat: String, long: String) {

        pb_loading.visibility = View.VISIBLE

        ApiUtilities.getApiInterface()?.getCurrentWeatherData(lat, long, API_KEY)?.enqueue(object :
            Callback<ModelClass> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                if (response.isSuccessful) {
                    setDataOnViews(response.body())
                }
            }

            override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }

        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnViews(body: ModelClass?) {

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        tv_date_time.text = currentDate

        max_temp.text = "Day" + kelvinToCelsius(body!!.main!!.temp_max) + "°"
        min_temp.text = "Night" + kelvinToCelsius(body!!.main!!.temp_min) + "°"
        tv_temp.text = "" + kelvinToCelsius(body!!.main!!.temp) + "°"
        feels_like.text = "Feels Like " + kelvinToCelsius(body!!.main!!.feels_like) + "°"
        tv_weather_type.text = body.weather[0].main
        tv_sunrise.text = timeStampToLocalDate(body.sys.sunrise.toLong())
        tv_sunset.text = timeStampToLocalDate(body.sys.sunset.toLong())
        tv_pressure.text = body.main.pressure.toString()
        tv_humidity.text = body.main.humidity.toString() + " %"
        tv_wind_speed.text = body.wind.speed.toString() + " m/s"

        tv_tempF.text = "" + kelvinToCelsius(body.main.temp).times(1.8).plus(32).toInt()
        et_get_city_name.setText(body.name)


        updateUI(body.weather[0].id)

    }

    private fun updateUI(id: Int) {

        if (id in 200..232) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.thunderstorm)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.thunderstorm))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.thunderstrom_bg)
            ll_main_bg_below.background =
                ContextCompat.getDrawable(this, R.drawable.thunderstrom_bg)
            ll_main_bg_above.background =
                ContextCompat.getDrawable(this, R.drawable.thunderstrom_bg)
            iv_weather_bg.setImageResource(R.drawable.thunderstrom_bg)
            iv_weather_icon.setImageResource(R.drawable.thunderstrom)
        } else if (id in 300..321) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.drizzle)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.drizzle))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.drizzle_bg)
            ll_main_bg_below.background = ContextCompat.getDrawable(this, R.drawable.drizzle_bg)
            ll_main_bg_above.background = ContextCompat.getDrawable(this, R.drawable.drizzle_bg)
            iv_weather_bg.setImageResource(R.drawable.drizzle_bg)
            iv_weather_icon.setImageResource(R.drawable.drizzle)
        } else if (id in 500..331) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.rain)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.rain))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.rainy_bg)
            ll_main_bg_below.background = ContextCompat.getDrawable(this, R.drawable.rainy_bg)
            ll_main_bg_above.background = ContextCompat.getDrawable(this, R.drawable.rainy_bg)
            iv_weather_bg.setImageResource(R.drawable.rainy_bg)
            iv_weather_icon.setImageResource(R.drawable.rain)
        } else if (id in 600..620) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.snow)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.snow))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.snow_bg)
            ll_main_bg_below.background = ContextCompat.getDrawable(this, R.drawable.snow_bg)
            ll_main_bg_above.background = ContextCompat.getDrawable(this, R.drawable.snow_bg)
            iv_weather_bg.setImageResource(R.drawable.snow_bg)
            iv_weather_icon.setImageResource(R.drawable.snow)
        } else if (id in 700..781) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.atmosphere)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.atmosphere))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.mist_bg)
            ll_main_bg_below.background = ContextCompat.getDrawable(this, R.drawable.mist_bg)
            ll_main_bg_above.background = ContextCompat.getDrawable(this, R.drawable.mist_bg)
            iv_weather_bg.setImageResource(R.drawable.mist_bg)
            iv_weather_icon.setImageResource(R.drawable.mist)
        } else if (id == 800) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clear)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.clear))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.clear_bg)
            ll_main_bg_below.background = ContextCompat.getDrawable(this, R.drawable.clear_bg)
            ll_main_bg_above.background = ContextCompat.getDrawable(this, R.drawable.clear_bg)
            iv_weather_bg.setImageResource(R.drawable.clear_bg)
            iv_weather_icon.setImageResource(R.drawable.clear)
        }else  {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clouds)
            rl_toolbar.setBackgroundColor(resources.getColor(R.color.clouds))
            rl_sub_layout.background = ContextCompat.getDrawable(this, R.drawable.clouds)
            ll_main_bg_below.background = ContextCompat.getDrawable(this, R.drawable.clouds)
            ll_main_bg_above.background = ContextCompat.getDrawable(this, R.drawable.clouds)
            iv_weather_bg.setImageResource(R.drawable.clouds)
            iv_weather_icon.setImageResource(R.drawable.clouds)
        }
        pb_loading.visibility = View.GONE
        rl_mainLayout.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalDate(timeStamp: Long): String {
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    private fun kelvinToCelsius(temp: Double): Double {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val API_KEY = "39b715f7c34ea3a690ef70fdbd5b4500"
    }

    private fun isLocationEnabled(): Boolean {
//      checks for gps or network
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

//       permission given
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}