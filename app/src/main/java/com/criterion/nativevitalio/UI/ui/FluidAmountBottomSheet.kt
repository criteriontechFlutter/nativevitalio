import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.SeekBar
import com.criterion.nativevitalio.databinding.LayoutBottomSheetFluidBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FluidAmountBottomSheet(
    private val onAdd: (Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutBottomSheetFluidBinding
    private var amount = 135

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutBottomSheetFluidBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateUI(amount)

        // Wait until layout is drawn before setting droplet position
        binding.seekBar.post {
            binding.seekBar.progress = amount
            animateDroplet(amount)

        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                amount = progress
                updateUI(progress)
                animateDroplet(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnAdd.setOnClickListener {
            onAdd(amount)
            dismiss()
        }
    }

    private fun updateUI(value: Int) {
        binding.tvQuantity.text = "$value"
        binding.btnAdd.text = "Add : $value ml"
    }

    private fun animateDroplet(progress: Int) {
        val seekBar = binding.seekBar
        val droplet = binding.dropletIcon

        val max = seekBar.max
        val seekBarWidth = seekBar.width - seekBar.paddingStart - seekBar.paddingEnd
        val percent = progress.toFloat() / max

        val thumbX = seekBar.paddingStart + (seekBarWidth * percent)
        val newX = thumbX - droplet.width / 2

        // Animate with bounce
        droplet.animate()
            .translationX(newX)
            .setDuration(200)
            .setInterpolator(BounceInterpolator())
            .start()
    }
}
