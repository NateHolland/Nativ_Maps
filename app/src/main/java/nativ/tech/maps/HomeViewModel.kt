package nativ.tech.maps

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nativ.tech.routes.GeoCalc
import nativ.tech.routes.Route
import java.io.InputStream

class HomeViewModel : ViewModel() {

    val importedRoute: MediatorLiveData<Event<Route>> = MediatorLiveData()
    val event: MediatorLiveData<Event<Event.Type>> = MediatorLiveData()

    fun importRoute(inputStream: InputStream?) {
        viewModelScope.launch(Dispatchers.IO) {
            GeoCalc.importGpx(inputStream)?.also {
                importedRoute.postValue(Event(it))
            }?:failed()
        }
    }

    private fun failed() {
        event.postValue(Event(Event.Type.IMPORT_FAILED))
    }
}