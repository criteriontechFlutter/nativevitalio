import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.critetiontech.ctvitalio.R

class DosageChartView(context: Context) : LinearLayout(context) {

    private val dp = resources.displayMetrics.density

    init {
        orientation = VERTICAL
        setPadding((32 * dp).toInt(), (32 * dp).toInt(), (32 * dp).toInt(), (32 * dp).toInt())
        setBackgroundColor(0xFFECF3F8.toInt())
        createDosageChart()
    }

    private fun createDosageChart() {
        val chartsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        chartsContainer.addView(
            createChart(
                iconRes = R.drawable.learn_icon,
                label = "Medicine",
                color = 0xFF4ECDC4.toInt(),
                values = listOf(0.15f, 0.85f, 0.9f, 0.85f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.25f, 0.2f, 0.15f, 0.1f, 0.05f, 0.0f),
                weight = 1f
            )
        )

        chartsContainer.addView(
            createChart(
                iconRes = R.drawable.steps_icon,
                label = "Blood test",
                color = 0xFFFF6B9D.toInt(),
                values = List(15) { 0f },
                weight = 1f
            )
        )

        addView(chartsContainer)
    }

    private fun createChart(
        iconRes: Int,
        label: String,
        color: Int,
        values: List<Float>,
        weight: Float
    ): View {
        val chartLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, weight)
            setPadding((16 * dp).toInt(), 0, (16 * dp).toInt(), 0)
        }

        /* HEADER */
        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, (16 * dp).toInt())
        }

        val iconView = ImageView(context).apply {
            setImageResource(iconRes)
            layoutParams = LayoutParams((24 * dp).toInt(), (24 * dp).toInt())
        }

        header.addView(iconView)
        header.addView(TextView(context).apply {
            text = label
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            setPadding((8 * dp).toInt(), 0, 0, 0)
        })

        chartLayout.addView(header)

        /* DOTS */
        val dots = LinearLayout(context).apply { orientation = HORIZONTAL }

        values.forEach { v ->
            dots.addView(View(context).apply {
                layoutParams = LayoutParams((10 * dp).toInt(), (10 * dp).toInt()).apply {
                    marginEnd = (4 * dp).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (v > 0) color else 0xFFBDBDBD.toInt())
                }
            })
        }

        chartLayout.addView(dots)

        /* BARS */
        val bars = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                (180 * dp).toInt()
            )
            gravity = android.view.Gravity.BOTTOM
        }

        values.forEach { v ->
            bars.addView(LinearLayout(context).apply {
                orientation = VERTICAL
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                setPadding((2 * dp).toInt(), 0, (2 * dp).toInt(), 0)

                addView(View(context).apply {
                    layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        (180 * dp * v).toInt()
                    )
                    setBackgroundColor(color)
                    alpha = 0.8f
                })
            })
        }

        chartLayout.addView(bars)

        return chartLayout
    }
    fun createBarsOnly(
        values: List<Float>,
        color: Int
    ): LinearLayout {

        val dp = resources.displayMetrics.density

        val barsLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                (70 * dp).toInt()
            )
            gravity = android.view.Gravity.BOTTOM
            setPadding(
                (4 * dp).toInt(),
                (6 * dp).toInt(),
                (4 * dp).toInt(),
                (6 * dp).toInt()
            )
        }

        val gap = (3 * dp).toInt()
        val barWidth = (8 * dp).toInt()   // << set width here

        values.forEach { value ->

            val bar = View(context).apply {

                // 🔥 fixed width with margin gap
                layoutParams = LayoutParams(
                    barWidth,
                    (70 * dp * value).toInt()
                ).apply {
                    marginStart = gap
                    marginEnd = gap
                }

                // 🔥 rounded corners
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = (8 * dp)
                    setColor(color)
                }

                alpha = 0.95f
            }

            barsLayout.addView(bar)
        }

        return barsLayout
    }

}