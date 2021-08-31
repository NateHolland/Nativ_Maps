package nativ.tech.maps

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.NonCancellable.cancel
import nativ.tech.routes.Route
import nativ.tech.routes.RouteEditView

class RouteEditFragment : Fragment() {

    companion object {
        fun newInstance() = RouteEditFragment()
    }

    private lateinit var viewModel: RouteEditViewModel
    private lateinit var routeEditView: RouteEditView

    private val args: RouteEditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.route_edit_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        routeEditView = view.findViewById(R.id.routeView)
        viewModel = ViewModelProvider(this).get(RouteEditViewModel::class.java)
        viewModel.event.observe(viewLifecycleOwner, Observer {
            when(it.getContent()) {
                Event.Type.ERROR -> showMessage(getString(R.string.error))
                Event.Type.CHOOSE_FILE_NAME -> chooseFileNameDialog()
                Event.Type.SAVE_SUCCESSFUL -> showMessage(getString(R.string.save_succesful))
            }
        })
        setRoute(args.route)
    }

    private fun setRoute(route: Route) {
        routeEditView.setRoute(route)
        route.name?.also {
            viewModel.setRouteName(it)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                viewModel.save(routeEditView.export())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.route_menu, menu)
    }

    private fun chooseFileNameDialog() {
        AlertDialog.Builder(context).also { builder ->
            builder.setTitle(R.string.choose_filename_title)
            EditText(context).also { input->
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)
                builder.setPositiveButton(R.string.save_file, DialogInterface.OnClickListener() { _, _ ->
                    input.text.toString().also {
                        routeEditView.setRouteName(it)
                        viewModel.setRouteName(it)
                        viewModel.save(routeEditView.export())
                    }
                })
                builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener() { dialog, _ ->
                    dialog.cancel()
                })
                builder.show()
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(routeEditView, message, Snackbar.LENGTH_LONG).show()
    }

}