package keronei.swapper.auth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class BleSignalStrengthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint objects for drawing
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f // Width of the circle stroke
        color = Color.parseColor("#E0E0E0") // Light gray for background circle
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f // Width of the circle stroke
        strokeCap = Paint.Cap.ROUND // Rounded ends for the progress arc
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18f // Default text size
        textAlign = Paint.Align.CENTER // Center the text horizontally
        color = Color.BLACK // Default text color
    }

    // Rectangle for the oval shape used in drawing the arc
    private val rectF = RectF()

    // Current RSSI value, default to 0 (no signal)
    var rssi: Int = 0
        set(value) {
            field = value
            // Request a redraw when RSSI changes
            invalidate()
        }

    // Constants for RSSI mapping
    private val MIN_RSSI = -100 // Very weak signal
    private val MAX_RSSI = -30  // Very strong signal

    /**
     * Calculates the signal strength as a percentage (0-100).
     * @param currentRssi The current RSSI value.
     * @return Percentage of signal strength.
     */
    fun getSignalPercentage(currentRssi: Int): Float {
        if (currentRssi == 0 || currentRssi < MIN_RSSI) {
            return 0f // No signal or extremely weak
        }
        if (currentRssi > MAX_RSSI) {
            return 100f // Strongest possible
        }
        // Map RSSI to a 0-100 percentage
        return ((currentRssi - MIN_RSSI).toFloat() / (MAX_RSSI - MIN_RSSI)) * 100f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Ensure the view is square
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        val size = min(width, height) // Take the smaller dimension to make it square
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Calculate the bounds for the circle
        val strokeWidth = progressPaint.strokeWidth
        rectF.set(
            strokeWidth / 2,
            strokeWidth / 2,
            w - strokeWidth / 2,
            h - strokeWidth / 2
        )
        // Adjust text size based on view size for better scaling
        textPaint.textSize = min(w, h) * 0.15f // Roughly 25% of the smaller dimension
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val percentage = getSignalPercentage(rssi)

        // Determine color and text based on signal strength
        val progressColor: Int
        val textColor: Int
        val displayText: String

        if (rssi == 0) { // No signal detected
            progressColor = Color.parseColor("#EF4444") // Red
            textColor = Color.parseColor("#DC2626") // Darker red
            displayText = "No Signal"
        } else if (percentage >= 50) { // Above 50% strength
            progressColor = Color.parseColor("#22C55E") // Green
            textColor = Color.parseColor("#16A34A") // Darker green
            displayText = "OK"
        } else { // Otherwise, display the signal strength
            progressColor = Color.parseColor("#F97316") // Orange
            textColor = Color.parseColor("#EA580C") // Darker orange
            displayText = "$rssi dBm"
        }

        progressPaint.color = progressColor
        textPaint.color = textColor

        // Draw background circle
        canvas.drawOval(rectF, backgroundPaint)

        // Draw progress arc
        val sweepAngle = percentage * 3.6f // 360 degrees / 100% = 3.6 degrees per percent
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint) // Start from top (-90 degrees)

        // Draw text in the center
        val xPos = width / 2f
        val yPos = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(displayText, xPos, yPos, textPaint)
    }
}