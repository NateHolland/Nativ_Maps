package nativ.tech.maps

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import nativ.tech.routes.Route

class BrowseRoutesFragment : Fragment() {

    companion object {
        fun newInstance() = BrowseRoutesFragment()
    }

    private lateinit var viewModel: BrowseRoutesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var noFiles: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.browse_routes_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(BrowseRoutesViewModel::class.java)
        viewModel.populate()
        recyclerView = view.findViewById(R.id.route_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        noFiles = view.findViewById(R.id.noRoutes)
        viewModel.fileList.observe(viewLifecycleOwner, Observer {
            showFiles(it)
        })
        viewModel.event.observe(viewLifecycleOwner, Observer {
            if (it.getContent() == Event.Type.ERROR) showMessage(getString(R.string.error))
        })
        viewModel.route.observe(viewLifecycleOwner, Observer {
            it.getContent()?.also { route ->
                val action = BrowseRoutesFragmentDirections.actionBrowseRoutesFragmentToRouteEditFragment(route)
                view.findNavController().navigate(action)
            }
        })
    }

    private fun showFiles(files: List<String>) {
        fun openFile(s: String) {
            viewModel.openFile(s)
        }
        fun deleteFile(s: String) {
            viewModel.deleteFile(s)
        }
        recyclerView.adapter = StringAdapter(files,::openFile,::deleteFile)
        if(files.isEmpty())noFiles()
    }

    private fun noFiles() {
        noFiles.visibility = View.VISIBLE
    }

    private fun showMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
    }

}