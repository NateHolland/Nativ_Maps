package nativ.tech.maps

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import nativ.tech.routes.Route
import java.io.File

class BrowseRoutesViewModel(application: Application) : AndroidViewModel(application) {

    val fileList: MediatorLiveData<List<String>> = MediatorLiveData()
    val event: MediatorLiveData<Event<Event.Type>> = MediatorLiveData()
    val route: MediatorLiveData<Event<Route>> = MediatorLiveData()

    fun populate() {
        routesFolder()?.list()?.also {list->
            fileList.value = list.filter {
                it.endsWith(".ntvmp")
            }.map {
                it.dropLast(6)
            }
        }?:failedToFetchFiles()
    }

    fun openFile(fileName: String){
        routesFolder()?.also { folder ->
            try {
                Route.fromFile(File("$folder/$fileName"))?.also {
                    route.value = Event(it)
                }?:error()
            }catch (e: Exception){
                e.printStackTrace()
                error()
            }
        }?:error()
    }

    private fun error() {
        event.value = Event(Event.Type.ERROR)
    }

    private fun failedToFetchFiles() {
        fileList.value = emptyList()
    }

}