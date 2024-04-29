import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.LeaderboardEntry
import com.example.mobileappscoursework.databinding.LeaderboardItemBinding
import androidx.core.content.ContextCompat
import com.example.mobileappscoursework.R


class LeaderboardAdapter(private val entries: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(private val binding: LeaderboardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntry) {
            binding.rank.text = entry.rank.toString()
            binding.playerName.text = entry.playerName
            binding.score.text = entry.score.toString()
        }

        // Method to set the trophy drawable
        fun setTrophyDrawable(drawable: Drawable?) {
            binding.playerName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LeaderboardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        holder.bind(entries[position])

        val endColor = ContextCompat.getColor(context, android.R.color.white)
        // Set background color based on rank
        val startColor = when (position) {
            0 -> ContextCompat.getColor(context, R.color.gold) // 1st place
            1 -> ContextCompat.getColor(context, R.color.silver) // 2nd place
            2 -> ContextCompat.getColor(context, R.color.bronze) // 3rd place
            else -> ContextCompat.getColor(context, R.color.default_background) // Default background
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(startColor, endColor)
        )

        // If you want to keep the original drawable (like a shape or border), you can layer it
        val backgroundDrawable = LayerDrawable(arrayOf(gradientDrawable))

        // Set the gradient as background
        holder.itemView.background = backgroundDrawable

        // Retrieve the trophy drawable and set it
        if (position in 0..2) {
            val trophyDrawable = ContextCompat.getDrawable(context, R.drawable.trophy_icon)?.apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }
            holder.setTrophyDrawable(trophyDrawable)
            // Other code to set padding if necessary
        } else {
            holder.setTrophyDrawable(null)
        }
    }


    override fun getItemCount() = entries.size
}
