package nativ.tech.maps

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import java.io.File

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
}