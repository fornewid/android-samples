package com.example.myapplication

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import android.widget.photopicker.EmbeddedPhotoPickerProviderFactory
import android.widget.photopicker.EmbeddedPhotoPickerSession
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.photopicker.EmbeddedPhotoPickerView
import androidx.photopicker.ExperimentalPhotoPickerApi
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.myapplication.databinding.ItemImageBinding
import com.example.myapplication.databinding.ViewSampleBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPhotoPickerApi::class)
class ViewSampleActivity : AppCompatActivity() {

    private val _attachments = MutableStateFlow(emptyList<Uri>())
    val attachments = _attachments.asStateFlow()

    private var openSession: EmbeddedPhotoPickerSession? = null
    private val pickerListener = object : EmbeddedPhotoPickerView.EmbeddedPhotoPickerStateChangeListener {
        override fun onSessionOpened(newSession: EmbeddedPhotoPickerSession) {
            openSession = newSession
        }

        override fun onSessionError(throwable: Throwable) {
        }

        override fun onUriPermissionGranted(uris: List<Uri>) {
            _attachments.value += uris
        }

        override fun onUriPermissionRevoked(uris: List<Uri>) {
            _attachments.value -= uris
        }

        override fun onSelectionComplete() {
        }
    }

    private var binding: ViewSampleBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ViewSampleBinding.inflate(layoutInflater)
        this.binding = binding
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.embeddedPhotoPicker.addEmbeddedPhotoPickerStateChangeListener(pickerListener)
        binding.embeddedPhotoPicker.setProvider(
            EmbeddedPhotoPickerProviderFactory.create(applicationContext)
        )
        binding.embeddedPhotoPicker.setEmbeddedPhotoPickerFeatureInfo(
            EmbeddedPhotoPickerFeatureInfo.Builder().build()
        )

        var showPhotoPicker = false
        binding.button.setOnClickListener {
            val wasShown = showPhotoPicker
            showPhotoPicker = !wasShown
            binding.embeddedPhotoPicker.isVisible = showPhotoPicker
            binding.button.text = if (showPhotoPicker) {
                "Hide Embedded Photo Picker"
            } else {
                "Show Embedded Photo Picker"
            }
        }

        val imageAdapter = SelectedImagesAdapter()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                attachments.collect {
                    imageAdapter.updateImages(images = it)
                    binding.tvAttachmentsCount.text = "Attachments count: ${it.size}"
                }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
    override fun onDestroy() {
        binding?.embeddedPhotoPicker?.removeEmbeddedPhotoPickerStateChangeListener(pickerListener)
        openSession = null
        super.onDestroy()
    }

    // RecyclerView Adapter for displaying selected images
    private class SelectedImagesAdapter : RecyclerView.Adapter<ViewHolder>() {
        private var images: List<Uri> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                binding = ItemImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(uri = images[position])
        }

        override fun getItemCount(): Int = images.size

        fun updateImages(images: List<Uri>) {
            this.images = images
            notifyDataSetChanged()
        }
    }

    private class ViewHolder(
        private val binding: ItemImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            binding.image.load(uri)
        }
    }
}
