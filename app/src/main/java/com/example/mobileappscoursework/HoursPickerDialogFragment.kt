package com.example.mobileappscoursework
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

class HoursPickerDialogFragment : DialogFragment() {

    interface HoursPickerDialogListener {
        fun onNumberPicked(number: Int)
    }

    var listener: HoursPickerDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val picker = NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = 20
            value = 1
        }

        builder.setView(picker)
        builder.setTitle("Select Hours Done")
        builder.setPositiveButton("Set") { dialog, which ->
            listener?.onNumberPicked(picker.value)
        }
        builder.setNegativeButton("Cancel", null)

        return builder.create()
    }
}