package nativ.tech.maps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StringAdapter(private val files: List<String>, private val openListener: (String) -> Unit, private val deleteListener: (String) -> Unit) : RecyclerView.Adapter<StringAdapter.StringViewHolder>() {

    class StringViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val deleteRoute: ImageView = view.findViewById(R.id.deleteRoute)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StringViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.file_listview, viewGroup, false)

        return StringViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: StringViewHolder, position: Int) {
        viewHolder.textView.text = files[position]
        viewHolder.textView.setOnClickListener { openListener(files[position]+".ntvmp") }
        viewHolder.deleteRoute.setOnClickListener {  deleteListener(files[position]+".ntvmp") }
    }

    override fun getItemCount() = files.size
}
