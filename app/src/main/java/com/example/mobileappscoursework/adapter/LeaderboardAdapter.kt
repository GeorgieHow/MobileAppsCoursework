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

        val startColor = when (position) {
            0 -> ContextCompat.getColor(context, R.color.gold)
            1 -> ContextCompat.getColor(context, R.color.silver)
            2 -> ContextCompat.getColor(context, R.color.bronze)
            else -> ContextCompat.getColor(context, R.color.default_background)
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(startColor, endColor)
        )


        val backgroundDrawable = LayerDrawable(arrayOf(gradientDrawable))

        holder.itemView.background = backgroundDrawable

        if (position in 0..2) {
            val trophyDrawable = ContextCompat.getDrawable(context, R.drawable.trophy_icon)?.apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }
            holder.setTrophyDrawable(trophyDrawable)
        } else {
            holder.setTrophyDrawable(null)
        }
    }


    override fun getItemCount() = entries.size
}
