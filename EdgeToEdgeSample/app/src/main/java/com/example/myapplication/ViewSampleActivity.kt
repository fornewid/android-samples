package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemColorBinding
import com.example.myapplication.databinding.ViewSampleBinding

class ViewSampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ViewSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
            )
            binding.recyclerView.updatePadding(
                bottom = systemBars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }

        val colors = List(10) {
            listOf(
                Color.BLACK,
                Color.BLUE,
                Color.CYAN,
                Color.DKGRAY,
                Color.GRAY,
                Color.GREEN,
                Color.LTGRAY,
                Color.MAGENTA,
                Color.RED,
                Color.WHITE,
                Color.YELLOW,
            )
        }.flatten()

        val spanCount = 3
        val adapter = SampleListAdapter()
        adapter.submitList(colors = colors)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
    }

    private class SampleListAdapter : RecyclerView.Adapter<SampleViewHolder>() {
        private var colors: List<Int> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
            return SampleViewHolder(
                binding = ItemColorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            )
        }

        override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
            holder.bind(color = colors[position])
        }

        override fun getItemCount(): Int = colors.size

        fun submitList(colors: List<Int>) {
            this.colors = colors
            notifyDataSetChanged()
        }
    }

    private class SampleViewHolder(
        private val binding: ItemColorBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: Int) {
            binding.color.setBackgroundColor(color)
        }
    }
}
