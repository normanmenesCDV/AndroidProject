package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Log
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
    private val monsterMarkers = mutableListOf<Marker>()
    private lateinit var monsterBitmapDescriptor: BitmapDescriptor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestLocationPermission()
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
            Log.d("XXX", "XXXXXXXXXXXX")
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location ->
                    if (location != null) {
                        currentLocation = location;
                        var newLocation = LatLng(location.latitude, location.longitude)
                        Log.d("XXX", "${currentLocation}")
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation))
                        googleMap.moveCamera(CameraUpdateFactory.zoomTo(18.0F))
                    }
                }
        }
    }
    override fun onMapReady(map: GoogleMap) {
        val cdv = LatLng(52.4155625, 16.9310632)
        currentLocation = Helpers().convertLatLngToLocation(cdv)

        googleMap = map
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cdv))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(18.0F))

        generateMonsters()
    }

    fun generateMonsters() {
        val radiusInKm = 5.0
        val numberOfPoints = 10
        val generatedCoordinates =
            currentLocation?.let { Helpers().generateRandomCoordinates(it, radiusInKm, numberOfPoints) }

        deleteExistMarkers()

        for (i in 0 until numberOfPoints) {
            val point = generatedCoordinates?.get(i)
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

    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun deleteExistMarkers()
    {
        monsterMarkers.forEach { it.remove() }
        monsterMarkers.clear()
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
