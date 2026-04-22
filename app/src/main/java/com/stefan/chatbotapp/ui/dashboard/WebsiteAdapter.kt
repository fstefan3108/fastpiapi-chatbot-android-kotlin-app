package com.stefan.chatbotapp.ui.dashboard
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.stefan.chatbotapp.R
import com.stefan.chatbotapp.data.models.WebsiteResponse

class WebsiteAdapter(
    private var websites: List<WebsiteResponse>,
    private val onDeleteClick: (WebsiteResponse) -> Unit
) : RecyclerView.Adapter<WebsiteAdapter.WebsiteViewHolder>() {

    // ViewHolder — holds references to all views in a single item
    inner class WebsiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvUrl: TextView = itemView.findViewById(R.id.tvUrl)
        val tvApiKey: TextView = itemView.findViewById(R.id.tvApiKey)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnToggleKey: Button = itemView.findViewById(R.id.btnToggleKey)
        val btnCopyKey: ImageButton = itemView.findViewById(R.id.btnCopyKey)

        // Each item tracks its own show/hide state
        var isKeyVisible: Boolean = false
    }

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebsiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_website, parent, false)
        return WebsiteViewHolder(view)
    }

    // Called to bind data to a ViewHolder — this is where the "recycling" happens
    override fun onBindViewHolder(holder: WebsiteViewHolder, position: Int) {
        val website = websites[position]

        holder.tvTitle.text = website.title
        holder.tvUrl.text = website.url

        // Reset visibility state when item is recycled
        holder.isKeyVisible = false
        holder.tvApiKey.text = "••••••••••••••••••••"
        holder.btnToggleKey.text = "Show"

        holder.btnToggleKey.setOnClickListener {
            holder.isKeyVisible = !holder.isKeyVisible
            if (holder.isKeyVisible) {
                holder.tvApiKey.text = website.api_key
                holder.tvApiKey.setTextColor(0xFF08C0DD.toInt())
                holder.btnToggleKey.text = "Hide"
            } else {
                holder.tvApiKey.text = "••••••••••••••••••••"
                holder.tvApiKey.setTextColor(0xFF999999.toInt())
                holder.btnToggleKey.text = "Show"
            }
        }

        holder.btnCopyKey.setOnClickListener {
            val clipboard = holder.itemView.context
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("API Key", website.api_key))
            Toast.makeText(holder.itemView.context, "API key copied", Toast.LENGTH_SHORT).show()
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(website)
        }
    }

    override fun getItemCount(): Int = websites.size

    // Efficient list updates using DiffUtil — only re-renders items that actually changed
    fun updateData(newWebsites: List<WebsiteResponse>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = websites.size
            override fun getNewListSize() = newWebsites.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                websites[oldPos].id == newWebsites[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                websites[oldPos] == newWebsites[newPos]
        })
        websites = newWebsites
        diff.dispatchUpdatesTo(this)
    }
}