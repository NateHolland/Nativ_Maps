package nativ.tech.routes

import android.graphics.Point
import android.graphics.PointF
import android.location.Location
import android.util.Log
import android.util.Xml
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class GeoCalc {
    companion object{

        suspend fun importGpx(stream: InputStream?): Route?{
            return withContext(Dispatchers.IO) {
                var imported:Route? = null
                stream?.also {
                    try {
                        GPXParser().parse(it).also { gpx ->
                            Route(ArrayList(),null).also { route ->
                                gpx.tracks.forEach { track ->
                                    track.trackSegments.forEach { trackSegment ->
                                        trackSegment.trackPoints.forEach { trackPoint ->
                                            route.path.add(GeoPoint(trackPoint.latitude,trackPoint.longitude))
                                        }
                                    }
                                }
                                imported = route
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: XmlPullParserException) {
                        e.printStackTrace()
                    }
                }
                imported
            }

        }

        suspend fun exportGpx(outputStream: OutputStream?, route: Route): Boolean {
            return withContext(Dispatchers.IO){
                var success = false
                outputStream?.also { stream ->
                    val writer = StringWriter()
                    Xml.newSerializer().run {
                        setOutput(writer)
                        startDocument("UTF-8",null)
                        startTag(null, "gpx")
                        attribute(null,"creator","Nativ Maps")
                        attribute(null,"xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance")
                        attribute(null,"xsi:schemaLocation","http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd")
                        attribute(null,"version","1.1")
                        attribute(null,"xmlns","http://www.topografix.com/GPX/1/1")
                        startTag(null,"metadata")
                        startTag(null,"time")
                        text(SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'").also { it.timeZone = TimeZone.getTimeZone("UTC") }.format(Date()))
                        endTag(null,"time")
                        endTag(null,"metadata")
                        startTag(null,"trk")
                        startTag(null,"name")
                        route.name?:"Nativ Maps Route: ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}.gpx"
                        endTag(null,"name")
                        startTag(null,"trkseg")
                        route.path.forEach { point->
                            startTag(null,"trkpt")
                            attribute(null,"lat","${point.latitude}")
                            attribute(null,"lon","${point.longitude}")
                            startTag(null,"ele")
                            text("${point.altitude}")
                            endTag(null,"ele")
                            endTag(null,"trkpt")
                        }
                        endTag(null,"trkseg")
                        endTag(null,"trk")
                        endTag(null,"gpx")
                        endDocument()
                        flush()
                    }
                    stream.write(writer.toString().toByteArray())
                    stream.close()
                    success = true
                }
                success
            }
        }

        private fun location(latitude: Double, longitude: Double, time: Long): Location {
            var loc = Location("")
            loc.latitude = latitude
            loc.longitude = longitude
            loc.time = time
            return loc
        }

        fun pointFromGeoPoint(gp: GeoPoint?, vw: MapView): Point? {
            return gp?.let {
                val rtnPoint = Point()
                val projection = vw.projection
                projection.toPixels(it, rtnPoint)
                // Get the top left GeoPoint
                val geoPointTopLeft = projection.fromPixels(0, 0) as GeoPoint
                val topLeftPoint = Point()
                // Get the top left Point (includes osmdroid offsets)
                projection.toPixels(geoPointTopLeft, topLeftPoint)
                rtnPoint.x -= topLeftPoint.x // remove offsets
                rtnPoint.y -= topLeftPoint.y
                Log.d("bounds nw",rtnPoint.toString())
                rtnPoint
            }
        }

        fun pathLength(path: ArrayList<GeoPoint>): Double {
            return if (path.size<2) 0.0
            else{
                var length = 0.0
                for (i in 1 until path.size)length += path[i-1].distanceToAsDouble(path[i])
                length
            }
        }

        fun contains(test: Point, points: ArrayList<Point>): Boolean {
            if (points.size<3)return false
            var result = false
            var i = 0
            var j = points.size - 1
            while (i < points.size) {
                if (points[i].y > test.y != points[j].y > test.y &&
                    test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x
                ) {
                    result = !result
                }
                j = i++
            }
            return result
        }

        fun findNearestPointLimitless(test: GeoPoint, target: ArrayList<GeoPoint>): GeoPoint {
            val points = GeoPoint(test.latitude,test.longitude)
            var mindist = -1.0
            for (i in target.indices) {
                val point: GeoPoint = target[i]
                var segmentPoint = i + 1
                if (segmentPoint >= target.size)break
                mindist = findNearestPointLimitless(test,point, target[segmentPoint]).let {
                    it.distanceToAsDouble(test).let {thisDist->
                        when {
                            mindist<0 ->{
                                points.setCoords(it.latitude,it.longitude)
                                thisDist
                            }
                            thisDist<mindist -> {
                                points.setCoords(it.latitude,it.longitude)
                                thisDist
                            }
                            else -> mindist
                        }
                    }

                }
            }
            return points
        }

        private fun distanceToLine(p: GeoPoint, start: GeoPoint, end: GeoPoint): Double {
            if (start == end) {
                return end.distanceToAsDouble(p)
            }
            val s0lat: Double = Math.toRadians(p.latitude)
            val s0lng: Double = Math.toRadians(p.longitude)
            val s1lat: Double = Math.toRadians(start.latitude)
            val s1lng: Double = Math.toRadians(start.longitude)
            val s2lat: Double = Math.toRadians(end.latitude)
            val s2lng: Double = Math.toRadians(end.longitude)
            val s2s1lat = s2lat - s1lat
            val s2s1lng = s2lng - s1lng
            val u = (((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                    / (s2s1lat * s2s1lat + s2s1lng * s2s1lng))
            if (u <= 0) {
                return p.distanceToAsDouble(start)
            }
            if (u >= 1) {
                return p.distanceToAsDouble(end)
            }
            val su = GeoPoint(
                start.latitude + u * (end.latitude - start.latitude),
                start.longitude + u * (end.longitude - start.longitude)
            )
            return p.distanceToAsDouble(su)
        }

        private fun findNearestPointLimitless(p: GeoPoint, start: GeoPoint, end: GeoPoint): GeoPoint {
            if (start == end) {
                return start
            }
            Projection(19.0,1000,1000,p, 0F,false,false,0,0).run {
                val a = PointF(this.toPixels(start as IGeoPoint,null))
                val b = PointF(this.toPixels(end as IGeoPoint,null))
                val c = PointF(this.toPixels(p as IGeoPoint,null))
                val dy = b.y-a.y
                val dx = b.x-a.x
                val pointF = when {
                    dy==0f -> PointF(c.x,a.y)
                    dx==0f -> PointF(a.x,c.y)
                    else -> {
                        val m = dy/dx
                        val perpM = -dx/dy
                        val perpCon = c.y-perpM*c.x
                        val con = a.y-m*a.x
                        val x = (con-perpCon)/(perpM-m)
                        val y = m*x+con
                        PointF(x,y)
                    }
                }
                val startEndDist = a.distTo(b)*1.0001f
                val endDist = b.distTo(pointF)
                val startDist = a.distTo(pointF)
                return if(startDist+endDist>startEndDist){
                    if(startDist<endDist)start
                    else end
                }else this.fromPixels(pointF.x.toInt(),pointF.y.toInt()) as GeoPoint
            }
        }

        private fun findNearestPoint(p: GeoPoint, start: GeoPoint, end: GeoPoint): GeoPoint {
            if (start == end) {
                return start
            }
            val s0lat = Math.toRadians(p.latitude)
            val s0lng = Math.toRadians(p.longitude)
            val s1lat = Math.toRadians(start.latitude)
            val s1lng = Math.toRadians(start.longitude)
            val s2lat = Math.toRadians(end.latitude)
            val s2lng = Math.toRadians(end.longitude)
            val s2s1lat = s2lat - s1lat
            val s2s1lng = s2lng - s1lng
            val u = (((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                    / (s2s1lat * s2s1lat + s2s1lng * s2s1lng))
            if (u <= 0) {
                return start
            }
            return if (u >= 1) {
                end
            } else GeoPoint(
                start.latitude + u * (end.latitude - start.latitude),
                start.longitude + u * (end.longitude - start.longitude)
            )
        }

    }
}

private fun PointF.flip(): PointF {
    return PointF(this.x,1000-this.y)
}

private fun PointF.distTo(pointF: PointF):Float {
    return sqrt((this.x-pointF.x)*(this.x-pointF.x)+(this.y-pointF.y)*(this.y-pointF.y))
}
