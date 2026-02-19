package com.example.fitnote.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitnote.R
import com.example.fitnote.data.api.RetrofitClient
import com.example.fitnote.ui.exercise.ExerciseDetailActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

enum class MarkerCategory {
    GYM, SWIM, STADIUM, PARK
}

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private val markerMap = mutableMapOf<MarkerCategory, MutableList<Marker>>()

    private lateinit var checkGym: CheckBox
    private lateinit var checkSwim: CheckBox
    private lateinit var checkPark: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        MarkerCategory.values().forEach {
            markerMap[it] = mutableListOf()
        }

        checkGym = findViewById(R.id.checkGym)
        checkSwim = findViewById(R.id.checkSwim)
        checkPark = findViewById(R.id.checkPark)

        // 체크박스 클릭 리스너 설정 (null 리스너 제거 후 재설정)
        checkGym.setOnCheckedChangeListener(null)
        checkSwim.setOnCheckedChangeListener(null)
        checkPark.setOnCheckedChangeListener(null)
        
        checkGym.setOnCheckedChangeListener { _, isChecked ->
            Log.d("FitNoteMap", "헬스 체크박스 클릭: $isChecked, 마커 수: ${markerMap[MarkerCategory.GYM]?.size ?: 0}")
            toggleMarkers(MarkerCategory.GYM, isChecked)
        }

        checkSwim.setOnCheckedChangeListener { _, isChecked ->
            Log.d("FitNoteMap", "수영 체크박스 클릭: $isChecked, 마커 수: ${markerMap[MarkerCategory.SWIM]?.size ?: 0}")
            toggleMarkers(MarkerCategory.SWIM, isChecked)
        }

        checkPark.setOnCheckedChangeListener { _, isChecked ->
            Log.d("FitNoteMap", "공원 체크박스 클릭: $isChecked, 마커 수: ${markerMap[MarkerCategory.PARK]?.size ?: 0}")
            toggleMarkers(MarkerCategory.PARK, isChecked)
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            showMockLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }

        googleMap.setOnMarkerClickListener { marker ->
            when (marker.tag as? MarkerCategory) {
                MarkerCategory.PARK -> {
                    startActivity(
                        Intent(this, ExerciseDetailActivity::class.java).apply {
                            putExtra("exerciseType", "러닝")
                            putExtra("placeName", marker.title)
                        }
                    )
                }
                else -> {
                    startActivity(
                        Intent(this, ExerciseDetailActivity::class.java).apply {
                            putExtra("exerciseType", "운동")
                            putExtra("placeName", marker.title)
                        }
                    )
                }
            }
            true
        }
    }

    private fun showMockLocation() {
        val location = LatLng(37.5665, 126.9780)

        googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("현재 위치")
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        searchSportsPlaces(location)
    }

    private fun searchSportsPlaces(center: LatLng) {
        // 네트워크 연결 확인
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this,
                "인터넷 연결을 확인해주세요.\n네트워크가 연결되면 운동 시설을 검색할 수 있습니다.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                searchAndDraw(center, "gym", "헬스",
                    BitmapDescriptorFactory.HUE_RED, MarkerCategory.GYM)

                searchAndDraw(center, "swimming_pool", "수영",
                    BitmapDescriptorFactory.HUE_BLUE, MarkerCategory.SWIM)

                searchAndDraw(center, "stadium", "체육",
                    BitmapDescriptorFactory.HUE_GREEN, MarkerCategory.STADIUM)

                searchAndDraw(center, "park", "공원",
                    BitmapDescriptorFactory.HUE_CYAN, MarkerCategory.PARK)

            } catch (e: java.net.UnknownHostException) {
                Log.e("FitNoteMap", "네트워크 연결 실패", e)
                Toast.makeText(
                    this@MapActivity,
                    "네트워크 연결에 실패했습니다.\n인터넷 연결을 확인해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: retrofit2.HttpException) {
                Log.e("FitNoteMap", "API 오류: ${e.code()}", e)
                Toast.makeText(
                    this@MapActivity,
                    "운동 시설 검색에 실패했습니다.\n잠시 후 다시 시도해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("FitNoteMap", "Places API 실패", e)
                Toast.makeText(
                    this@MapActivity,
                    "운동 시설을 불러오는 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private suspend fun searchAndDraw(
        center: LatLng,
        type: String,
        keyword: String?,
        color: Float,
        category: MarkerCategory
    ) {
        try {
            val response = RetrofitClient.placesNearbyApi.searchNearby(
                location = "${center.latitude},${center.longitude}",
                radius = 1500,
                type = type,
                keyword = keyword,
                apiKey = getString(R.string.google_maps_key)
            )

            Log.d("FitNoteMap", "$category 결과 수: ${response.results.size}")

            if (response.results.isEmpty()) {
                Log.d("FitNoteMap", "$category: 검색 결과 없음")
            }

            response.results.forEach { place ->
                val pos = LatLng(
                    place.geometry.location.lat,
                    place.geometry.location.lng
                )

                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(place.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                )

                if (marker != null) {
                    marker.tag = category
                    markerMap[category]?.add(marker)
                    Log.d("FitNoteMap", "마커 추가: ${place.name}, 카테고리: $category")
                } else {
                    Log.w("FitNoteMap", "마커 생성 실패: ${place.name}")
                }
            }

            // ✅ 핵심: 마커 추가 후 현재 체크 상태 다시 적용
            // UI 스레드에서 실행하여 체크박스 상태 확인
            runOnUiThread {
                toggleMarkers(category, isCategoryChecked(category))
            }
        } catch (e: Exception) {
            Log.e("FitNoteMap", "$category 검색 실패", e)
            // 개별 카테고리 실패는 상위에서 처리
            throw e
        }
    }

    private fun toggleMarkers(category: MarkerCategory, visible: Boolean) {
        val markers = markerMap[category]
        Log.d("FitNoteMap", "$category 마커 토글: visible=$visible, 마커 수=${markers?.size ?: 0}")
        if (markers == null || markers.isEmpty()) {
            Log.w("FitNoteMap", "$category 마커가 없습니다.")
            return
        }
        markers.forEach { marker ->
            if (marker != null) {
                marker.isVisible = visible
                Log.d("FitNoteMap", "마커 ${marker.title} visibility: $visible")
            }
        }
    }

    private fun isCategoryChecked(category: MarkerCategory): Boolean {
        return when (category) {
            MarkerCategory.GYM -> checkGym.isChecked
            MarkerCategory.SWIM -> checkSwim.isChecked
            MarkerCategory.PARK -> checkPark.isChecked
            MarkerCategory.STADIUM -> true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨
                if (::googleMap.isInitialized) {
                    googleMap.isMyLocationEnabled = true
                    showMockLocation()
                }
            } else {
                // 권한 거부됨
                Toast.makeText(
                    this,
                    "위치 권한이 필요합니다.\n설정에서 위치 권한을 허용해주세요.",
                    Toast.LENGTH_LONG
                ).show()
                // 권한 없이도 지도는 표시 (위치만 안 보임)
                if (::googleMap.isInitialized) {
                    showMockLocation()
                }
            }
        }
    }
}
