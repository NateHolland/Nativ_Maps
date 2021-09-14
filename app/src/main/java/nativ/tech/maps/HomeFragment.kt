package nativ.tech.maps

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import nativ.tech.routes.GeoCalc
import nativ.tech.routes.Route

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var progressBar: ContentLoadingProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ExtendedFloatingActionButton>(R.id.newRoute).setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToRouteEditFragment(Route(ArrayList(),null))
            view.findNavController().navigate(action)
        }
        view.findViewById<ExtendedFloatingActionButton>(R.id.brosweRoutes).setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToBrowseRoutesFragment()
            view.findNavController().navigate(action)
        }
        view.findViewById<ExtendedFloatingActionButton>(R.id.importRoute).setOnClickListener {
            getContent.launch("*/*")
        }
        progressBar = view.findViewById(R.id.progressBar)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.importedRoute.observe(viewLifecycleOwner, Observer {
            it.getContent()?.also {route ->
                progressBar.hide()
                val action = HomeFragmentDirections.actionHomeFragmentToRouteEditFragment(route)
                view?.findNavController()?.navigate(action)
            }
        })
        viewModel.event.observe(viewLifecycleOwner, Observer {
            when(it.getContent()) {
                Event.Type.IMPORT_FAILED -> importFailed()
            }
        })
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        progressBar.show()
        uri?.also {
            viewModel.importRoute(context?.contentResolver?.openInputStream(uri))
        }?:importFailed()
    }

    private fun importFailed() {
        showMessage(getString(R.string.import_failed))
        progressBar.hide()
    }

    private fun showMessage(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

}