package nativ.tech.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nativ.tech.routes.GeoCalc
import nativ.tech.routes.Route
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class RouteEditViewModel(application: Application) : AndroidViewModel(application) {

    private var fileName: String? = null
    val event: MediatorLiveData<Event<Event.Type>> = MediatorLiveData()

    fun save(route: String) {
        fileName?.also { name->
            routesFolder()?.also { folder ->
                try {
                    File(folder,name).also {
                        it.writeText(route)
                        event.value = Event(Event.Type.SAVE_SUCCESSFUL)
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    error()
                }
            }?:error()
        }?:setFileName()
    }

    private fun error() {
        event.value = Event(Event.Type.ERROR)
    }

    private fun setFileName() {
        event.value = Event(Event.Type.CHOOSE_FILE_NAME)
    }

    fun setRouteName(name: String) {
        fileName = "$name.ntvmp"
    }

    fun getRouteName(): String {
        return if(fileName.isNullOrEmpty()) "${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}.gpx"
        else "${fileName?.dropLast(6)}.gpx"
    }

    fun exportGPX(outputStream: OutputStream?, route: Route) {
        viewModelScope.launch(Dispatchers.IO) {
            if(GeoCalc.exportGpx(outputStream,route))event.postValue(Event(Event.Type.EXPORT_SUCEEDED))
            else event.postValue(Event(Event.Type.EXPORT_FAILED))
        }
    }
}