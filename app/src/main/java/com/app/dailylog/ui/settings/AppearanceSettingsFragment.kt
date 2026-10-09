package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.dailylog.R
import com.app.dailylog.databinding.AppearanceSettingsViewBinding
import com.app.dailylog.utils.EditorTextSize
import com.google.android.material.slider.Slider

/**
 * How the log editor looks. Split out of Settings so its controls don't compete for height
 * with the shortcut list; the preview can be two lines here, which it could not be inline.
 */
class AppearanceSettingsFragment(
    private val viewModel: SettingsViewModel
) : Fragment() {

    private lateinit var binding: AppearanceSettingsViewBinding

    companion object {
        fun newInstance(viewModel: SettingsViewModel) = AppearanceSettingsFragment(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.appearance_settings_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AppearanceSettingsViewBinding.bind(view)
        applySettingsInsets(view, binding.appearanceToolbar)
        useDarkStatusBarIcons(requireActivity().window)
        binding.appearanceToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        setUpEditorTextSize()
    }

    override fun onResume() {
        super.onResume()
        useDarkStatusBarIcons(requireActivity().window)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreThemeStatusBarIcons(requireActivity().window)
    }

    private fun setUpEditorTextSize() {
        val slider = binding.editorTextSizeSlider
        slider.valueFrom = EditorTextSize.MIN.toFloat()
        slider.valueTo = EditorTextSize.MAX.toFloat()
        slider.stepSize = EditorTextSize.STEP.toFloat()
        slider.value = (viewModel.getEditorTextSize()?.let { EditorTextSize.snap(it.toFloat()) }
            ?: EditorTextSize.nearestStep(requireContext())).toFloat()
        // fromUser only: setting the slider for Reset must not save a size.
        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                chooseEditorTextSize(value)
            }
        }
        // A tap on the step the thumb already shows changes nothing, so it would otherwise never save.
        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) = chooseEditorTextSize(slider.value)
        })
        binding.editorTextSizeReset.setOnClickListener {
            viewModel.setEditorTextSize(null)
            slider.value = EditorTextSize.nearestStep(requireContext()).toFloat()
            renderEditorTextSize()
        }
        renderEditorTextSize()
    }

    private fun chooseEditorTextSize(value: Float) {
        viewModel.setEditorTextSize(value.toInt())
        renderEditorTextSize()
    }

    private fun renderEditorTextSize() {
        val size = viewModel.getEditorTextSize()
        binding.editorTextSizeValue.text =
            size?.toString() ?: getString(R.string.editor_text_size_default)
        binding.editorTextSizeReset.isEnabled = size != null
        EditorTextSize.apply(binding.editorTextSizePreview, size)
    }
}
