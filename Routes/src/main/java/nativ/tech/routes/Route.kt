package nativ.tech.routes

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint
import java.io.File

@Parcelize
data class Route(
    @SerializedName("path") val path: ArrayList<GeoPoint>,
    @SerializedName("name") var name: String?
) : Parcelable {
    override fun toString(): String {
        return Gson().toJson(this)
    }
    companion object{
        fun fromFile(file: File): Route? {
            return Gson().fromJson((file).readLines().joinToString { it },Route::class.java)
        }
    }
}

