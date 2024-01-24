package com.example.myapplication

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Helpers {
    fun generateRandomCoordinates(center: Location, radius: Double, numberOfPoints: Int): List<Location> {
        val random = java.util.Random()
        val generatedPoints = mutableListOf<Location>()

        for (i in 0 until numberOfPoints) {

            val radiusInDegrees = radius / 111.32

            val u = random.nextDouble()
            val v = random.nextDouble()
            val w = radiusInDegrees * sqrt(u)
            val t = 2.0 * PI * v
            val x = w * cos(t)
            val y = w * sin(t)


            val new_x = x / cos(center.latitude * (PI / 180.0))

            val newLatitude = center.latitude + y
            val newLongitude = center.longitude + new_x

            val newLocation = Location("")
            newLocation.latitude = newLatitude
            newLocation.longitude = newLongitude
            generatedPoints.add(newLocation)
        }

        return generatedPoints
    }

    fun convertLatLngToLocation(latLng: LatLng): Location {
        val location = Location("ConvertedLocation")
        location.latitude = latLng.latitude
        location.longitude = latLng.longitude
        return location
    }
}