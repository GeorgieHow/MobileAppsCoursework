import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

class NumberPickerDialogFragment : DialogFragment() {

    interface NumberPickerDialogListener {
        fun onNumberPicked(number: Int)
    }

    var listener: NumberPickerDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val picker = NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = 100
            value = 50
        }

        builder.setView(picker)
        builder.setTitle("Choose your goal")
        builder.setPositiveButton("Set") { dialog, which ->
            listener?.onNumberPicked(picker.value)
        }
        builder.setNegativeButton("Cancel", null)

        return builder.create()
    }
}
