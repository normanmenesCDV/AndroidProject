package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.MapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: MapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: Location? = null
    private var monstersLocation: List<Location>? = null
    private var locationMarkers: Marker? = null
    private var monsterMarkers = mutableListOf<Marker>()
    private lateinit var monsterBitmapDescriptor: BitmapDescriptor
    private lateinit var pointBitmapDescriptor: BitmapDescriptor

    private lateinit var notificationHelper: Notification

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val cdvLocation = Helpers().convertLatLngToLocation(LatLng(52.4155625, 16.9310632))
        currentLocation = cdvLocation
        requestLocationPermission()

        _binding = MapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        monsterBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.monster)
        pointBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.point)

        requestLocationPermission()
        notificationHelper = Notification(requireContext())
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            updateLocation()
        }
    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        else {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location ->
                    if (location != null) {
                        currentLocation = location;
                        setCamera(currentLocation!!, 18.0F)
                        addCurrentLocationMarker(currentLocation!!)
                        generateMonsters()
                        showNotification()
                    }
                }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun setCamera(location: Location, zoom: Float) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom))
    }
    private fun addCurrentLocationMarker(location: Location) {
        locationMarkers = null
        //googleMap.clear()
        val latLng = LatLng(location.latitude, location.longitude)

        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Moja aktualna lokalizacja")
            .icon(pointBitmapDescriptor)

        locationMarkers = googleMap.addMarker(markerOptions)
        locationMarkers?.showInfoWindow()
    }

    fun generateMonsters() {
        monstersLocation = Helpers().generateListLocationsNearNocation(currentLocation!!, 5.0, 10)

        monsterMarkers.forEach { it.remove() }
        monsterMarkers.clear()

        for (i in monstersLocation?.indices!!) {
            val point = monstersLocation?.get(i)
            if (point != null) {

                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(point.latitude, point.longitude))
                        .title("Monster ${i + 1}")
                        .icon(monsterBitmapDescriptor)
                        .anchor(0.5f, 0.5f)
                )
                if (marker != null) {
                    monsterMarkers.add(marker)
                }
            }
        }
    }

    fun showNotification() {
        if (currentLocation != null && monstersLocation != null) {
            val closestMonster = Helpers().findClosestMonster(currentLocation!!, monstersLocation!!)
            if (closestMonster != null) {
                val distanceInMeters = Helpers().calculateDistanceInMeters(currentLocation!!, closestMonster)
                notificationHelper.showNotification("Uwaga", "Najbliższy potwór jest oddalony o $distanceInMeters metrów.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
