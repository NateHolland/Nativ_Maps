package nativ.tech.maps

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import java.io.File

fun AndroidViewModel.routesFolder(): File? {
    return getApplication<Application>().applicationContext.getDir("routes", Context.MODE_PRIVATE)
}