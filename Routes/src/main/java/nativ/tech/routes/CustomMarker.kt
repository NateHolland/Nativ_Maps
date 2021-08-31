package nativ.tech.routes

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class CustomMarker(map: MapView): Marker(map) {
    lateinit var onClick: (CustomMarker, MapView)->Boolean
}
