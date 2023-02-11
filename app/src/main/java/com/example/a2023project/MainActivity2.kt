package com.example.a2023project

import androidx.databinding.DataBindingUtil.setContentView
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.graphics.Point
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.a2023project.component.Common
import com.example.a2023project.data.ModelWeather
import com.example.a2023project.databinding.ActivityMain2Binding
import com.example.a2023project.databinding.ActivityMainBinding
import com.example.a2023project.network.WeatherObject
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class MainActivity2 : AppCompatActivity() {

    private var baseDate = "20230212"  // 발표 일자
    private var baseTime = "1400"      // 발표 시각
    private var curPoint: Point? = null    // 현재 위치의 격자 좌표를 저장할 포인트


    lateinit var binding: ActivityMain2Binding

    @SuppressLint("SetTextI18n", "MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main2)
        binding.mainActivity2=this@MainActivity2

        // Get permission
        val permissionList = arrayOf<String>(
            // 위치 권한
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        // 권한 요청
        ActivityCompat.requestPermissions(this@MainActivity2, permissionList, 1)

        val item : ModelWeather? = null

        // 오늘 날짜, 기온 텍스트뷰, 날씨 이미지뷰 설정
        binding.tvDate.text = SimpleDateFormat(
            "yyyy년 M월 d일 E요일 HH:mm",
            Locale.getDefault()
        ).format(Calendar.getInstance().time)
        binding.tvTemp.text = "12°C"
        binding.imgWeather.setImageResource(getRainImage("5", "4"))

        requestLocation()


    }


    // 날씨 가져와서 설정하기
    private fun setWeather(nx: Int, ny: Int) {
        // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
        // 현재 날짜, 시간 정보 가져오기
        val cal = Calendar.getInstance()
        baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시각
        val timeM = SimpleDateFormat("mm", Locale.getDefault()).format(cal.time) // 현재 분
        // API 가져오기 적당하게 변환
        baseTime = Common().getBaseTime(timeH, timeM)
        // 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if (timeH == "00" && baseTime == "2330") {
            cal.add(Calendar.DATE, -1).toString()
            baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        // 날씨 정보 가져오기
        // (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날짜, 발표 시각, 예보지점 좌표)
        val call =
            WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", baseDate, baseTime, nx, ny)

    }

    // 내 현재 위치의 위경도를 격자 좌표로 변환하여 해당 위치의 날씨정보 설정하기
    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity2)

        try {
            // 나의 현재 위치 요청
            val locationRequest = LocationRequest.create()
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 60 * 1000    // 요청 간격(1초)
            }
            val locationCallback = object : LocationCallback() {
                // 요청 결과
                override fun onLocationResult(p0: LocationResult) {
                    p0.let {
                        for (location in it.locations) {


                            // 현재 위치의 위경도를 격자 좌표로 변환
                            curPoint = Common().dfsXyConv(location.latitude, location.longitude)

                            // 오늘 날짜 텍스트뷰 설정
                            binding.tvDate.text = SimpleDateFormat(
                                "yyyy년 M월 d일 E요일 HH:mm",
                                Locale.getDefault()
                            ).format(Calendar.getInstance().time)
                            // nx, ny지점의 날씨 가져와서 설정하기
                            setWeather(curPoint!!.x, curPoint!!.y)
                        }
                    }
                }
            }

            // 내 위치 실시간으로 감지
            Looper.myLooper()?.let {
                locationClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    it
                )
            }


        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun getRainImage(rainType : String, sky: String) : Int {
        return when(rainType) {
            "0" -> getWeatherImage(sky)
            "1" -> R.drawable.rainy
            "2" -> R.drawable.hail
            "3" -> R.drawable.snowy
            "4" -> R.drawable.brash
            else -> getWeatherImage(sky)
        }
    }

    fun getWeatherImage(sky : String) : Int {
        // 하늘 상태
        return when(sky) {
            "1" -> R.drawable.sun                       // 맑음
            "3" ->  R.drawable.cloudy                     // 구름 많음
            "4" -> R.drawable.blur                 // 흐림
            else -> R.drawable.ic_launcher_foreground   // 오류
        }
    }


}