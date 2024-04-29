package com.example.mobileappscoursework.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobileappscoursework.LoggingChunksActivity
import com.example.mobileappscoursework.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import com.example.mobileappscoursework.R


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val loggingButton: Button = binding.logItButton
        loggingButton.setOnClickListener {
            val intent = Intent(requireContext(), LoggingChunksActivity::class.java)
            startActivity(intent)
        }

        val lineChart: LineChart = binding.graphLineChart

        val entries = ArrayList<Entry>()
    // Assuming you have a data source, add entries to the list
    // The Entry constructor takes two parameters: the first is the X value, and the second is the Y value.
        entries.add(Entry(0f, 4f)) // Monday
        entries.add(Entry(1f, 2f)) // Tuesday
        entries.add(Entry(2f, 4f)) // Wednesday
        entries.add(Entry(3f, 7f)) // Thursday
        entries.add(Entry(4f, 9f)) // Friday
        entries.add(Entry(5f, 6f)) // Saturday
        entries.add(Entry(6f, 3f)) // Sunday

        val dataSet = LineDataSet(entries, "Hours spent")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.BLUE)

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.text = "Hours logged each day"
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.setLabelCount(7, true) // Set to the number of days in the week
        lineChart.xAxis.labelRotationAngle = -45f

        lineChart.invalidate()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}