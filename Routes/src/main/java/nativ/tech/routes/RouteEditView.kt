package nativ.tech.routes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import java.util.logging.Handler

class RouteEditView: ConstraintLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,attrs,defStyleAttr){
        initView()
    }

    private lateinit var map: MapView
    private lateinit var drawLayer: DrawView
    private var currentPoint:GeoPoint? = null
    private var lastPoint:GeoPoint? = null
    private lateinit var route: Route
    private val editRoutePointOverlays = ArrayList<Overlay>()
    private val routeLine = Polyline()
    private val drawingLine = Polyline()
    private lateinit var addPoint: ExtendedFloatingActionButton
    private lateinit var duplicatePoint: ExtendedFloatingActionButton
    private lateinit var deletePoint: ExtendedFloatingActionButton

    private val deselectedPinDrawable by lazy { BitmapDrawable(resources, Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.pin_deselected), ((16.0f * resources.displayMetrics.density).toInt()), ((23.7f * resources.displayMetrics.density).toInt()), true))
    }
    private val selectedPinDrawable  by lazy { BitmapDrawable(resources, Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.pin_selected), ((16.0f * resources.displayMetrics.density).toInt()), ((23.7f * resources.displayMetrics.density).toInt()), true))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        Configuration.getInstance().userAgentValue = "nativ.tech.route"
        inflate(context,R.layout.route_edit_layout,this)
        map = findViewById(R.id.map)
        drawLayer = findViewById(R.id.drawLayer)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        context?.also {
            drawingLine.outlinePaint.color = ContextCompat.getColor(it, R.color.drawing_polygon_colour)
        }
        addPoint = findViewById(R.id.addPoint)
        addPoint.setOnClickListener { addPoint() }
        duplicatePoint = findViewById(R.id.duplicatePoint)
        duplicatePoint.setOnClickListener {
            duplicateSelectedPoint()
        }
        deletePoint = findViewById(R.id.deletePoint)
        deletePoint.setOnClickListener {
            removeSelectedPoints()
            drawRoute()
        }
        map.overlayManager.add(routeLine)
        map.overlayManager.add(drawingLine)
        map.addMapListener(object : MapListener {
            override fun onZoom(arg0: ZoomEvent): Boolean {
                return false
            }
            override fun onScroll(scrollEvent: ScrollEvent): Boolean {
                scrollTo(map.mapCenter as GeoPoint)
                return false
            }
        })
        map.setOnTouchListener { _, event ->
            if(event.action== MotionEvent.ACTION_UP)(editRoutePointOverlays).forEach{ marker ->
                (marker as  CustomMarker).also {
                    if(it.hitTest(event, map))it.onClick(it,map)
                }
            }
            false
        }
    }

    private fun scrollTo(centre: GeoPoint) {
        currentPoint = centre
        drawDottedLine()
    }

    private fun addPoint() {
        currentPoint?.let {
            lastPoint?.also { lastVal-> if(route.path.isEmpty())route.path.add(lastVal)}
            lastPoint = it
            drawDottedLine()
            route.path.add(it)
            drawRoute()
        }
    }

    fun removeSelectedPoints() {
        route.path.removeAll(selectedMarkers)
        selectedMarkers.clear()
        checkMarkers()
        drawRoute()
    }

    fun duplicateSelectedPoint() {
        if(selectedMarkers.isNotEmpty())selectedMarkers.first().also { point->
            val projectedPoint = map.projection.toPixels(point,null)
            val shiftedPoint = map.projection.fromPixels(projectedPoint.x+10,projectedPoint.y+10)
            route.path.add(route.path.indexOf(point), shiftedPoint as GeoPoint)
        }
        selectedMarkers.clear()
        checkMarkers()
        drawRoute()
    }

    private fun drawRoute() {
        routeLine.setPoints(route.path)
        map.overlayManager.removeAll(editRoutePointOverlays)
        editRoutePointOverlays.clear()
        route.path.distinct().also {
            drawingLine.setPoints(it)
            it.forEach { point ->
                CustomMarker(map).let { marker ->
                    marker.position = point
                    marker.isDraggable = true
                    map.overlayManager.add(marker)
                    marker.icon = deselectedPinDrawable
                    editRoutePointOverlays.add(marker)
                    marker.setOnMarkerClickListener { _, _ -> true }
                    marker.onClick = { marker, mapView ->
                        marker.icon = if (markerSelected(point)) selectedPinDrawable else deselectedPinDrawable
                        checkMarkers()
                        mapView.invalidate()
                        true
                    }
                    marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDragEnd(marker: Marker?) {
                            drawLayer.showCentre = true
                            drawRoute()
                            checkMarkers()
                        }

                        override fun onMarkerDragStart(marker: Marker?) {
                            selectedMarkers.clear()
                            drawLayer.dashedLine = null
                            drawLayer.showCentre = false
                            drawLayer.invalidate()
                        }

                        override fun onMarkerDrag(marker: Marker?) {
                            marker?.also { draggedMarker ->
                                point.setCoords(
                                    draggedMarker.position.latitude,
                                    draggedMarker.position.longitude
                                )
                                drawingLine.setPoints(it)
                                map.invalidate()
                            }
                        }
                    })
                }
            }
        }
        map.invalidate()
    }

    private val selectedMarkers = ArrayList<GeoPoint>()
    private fun markerSelected(point: GeoPoint): Boolean {
        return if(selectedMarkers.contains(point)){
            selectedMarkers.remove(point)
            false
        }else{
            selectedMarkers.add(point)
            true
        }

    }

    private fun checkMarkers() {
        if(selectedMarkers.isEmpty()){
            drawLayer.showCentre = true
            scrollTo(map.mapCenter as GeoPoint)
            addPoint.visibility = VISIBLE
            duplicatePoint.visibility = GONE
            deletePoint.visibility = GONE
        }else{
            drawLayer.dashedLine = null
            drawLayer.showCentre = false
            if (selectedMarkers.size > 1){
                addPoint.visibility = GONE
                duplicatePoint.visibility = GONE
                deletePoint.visibility = VISIBLE
            }else{
                addPoint.visibility = GONE
                duplicatePoint.visibility = VISIBLE
                deletePoint.visibility = VISIBLE
            }
        }
        drawLayer.invalidate()
    }

    private fun drawDottedLine() {
        lastPoint?.also {point->
            GeoCalc.pointFromGeoPoint(point,map)?.let {
                drawLayer.dashedLine = Circle(it.x.toFloat(),it.y.toFloat(),0F)
                drawLayer.invalidate()
            }
        }

    }

    fun export(): String {
        return route.toString()
    }

    fun setRoute(route: Route) {
        this.route = route
        route.path.lastOrNull()?.also {
            lastPoint = it
            map.controller.animateTo(it,18.0,5)
        }?:map.controller.animateTo(GeoPoint(60.204186, 24.897866),18.0,5)
        drawRoute()
    }

    fun setRouteName(it: String) {
        route.name = it
    }

    fun getRoute(): Route {
        return route
    }
}