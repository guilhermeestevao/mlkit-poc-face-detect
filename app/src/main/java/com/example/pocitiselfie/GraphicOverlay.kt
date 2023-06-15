package com.example.pocitiselfie

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import kotlin.math.ceil

open class GraphicOverlay(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()
    var mScale: Float? = null
    var mOffsetX: Float? = null
    var mOffsetY: Float? = null
    var cameraSelector: Int = CameraSelector.LENS_FACING_FRONT
    lateinit var processBitmap: Bitmap
    lateinit var processCanvas: Canvas

    abstract class Graphic(private val overlay: GraphicOverlay) {

        abstract fun draw(canvas: Canvas?)

        fun calculateRect(height: Float, width: Float, boundingBoxT: Rect): RectF {

            // for land scape
            fun isLandScapeMode(): Boolean {
                return overlay.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }

            fun whenLandScapeModeWidth(): Float {
                return when(isLandScapeMode()) {
                    true -> width
                    false -> height
                }
            }

            fun whenLandScapeModeHeight(): Float {
                return when(isLandScapeMode()) {
                    true -> height
                    false -> width
                }
            }

            val scaleX = overlay.width.toFloat() / whenLandScapeModeWidth()
            val scaleY = overlay.height.toFloat() / whenLandScapeModeHeight()
            val scale = scaleX.coerceAtLeast(scaleY)
            overlay.mScale = scale

            // Calculate offset (we need to center the overlay on the target)
            val offsetX = (overlay.width.toFloat() - ceil(whenLandScapeModeWidth() * scale)) / 2.0f
            val offsetY = (overlay.height.toFloat() - ceil(whenLandScapeModeHeight() * scale)) / 2.0f

            overlay.mOffsetX = offsetX
            overlay.mOffsetY = offsetY

            val mappedBox = RectF().apply {
                left = boundingBoxT.right * scale + offsetX
                top = boundingBoxT.top * scale + offsetY
                right = boundingBoxT.left * scale + offsetX
                bottom = boundingBoxT.bottom * scale + offsetY
            }

            // for front mode
            if (overlay.isFrontMode()) {
                val centerX = overlay.width.toFloat() / 2
                mappedBox.apply {
                    left = centerX + (centerX - left)
                    right = centerX - (right - centerX)
                }
            }
            return mappedBox
        }

        fun translateX(horizontal: Float): Float {
            return if (overlay.mScale != null && overlay.mOffsetX != null && !overlay.isFrontMode()) {
                (horizontal * overlay.mScale!!) + overlay.mOffsetX!!
            } else if (overlay.mScale != null && overlay.mOffsetX != null && overlay.isFrontMode()) {
                val centerX = overlay.width.toFloat() / 2
                centerX - ((horizontal * overlay.mScale!!) + overlay.mOffsetX!! - centerX)
            } else {
                horizontal
            }
        }

        fun translateY(vertical: Float): Float {
            return if (overlay.mScale != null && overlay.mOffsetY != null) {
                (vertical * overlay.mScale!!) + overlay.mOffsetY!!
            } else {
                vertical
            }
        }

    }

    fun isFrontMode() = cameraSelector == CameraSelector.LENS_FACING_FRONT

    fun toggleSelector() {
        cameraSelector =
            if (cameraSelector == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
    }

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }
    private fun drawBackgroundShape(canvas: Canvas?) {
        val paint = Paint()
        val backgroundAlpha = 0.8
        paint.color = ColorUtils.setAlphaComponent(context?.let {
            ContextCompat.getColor(
                it,
                R.color.overlay
            )
        }!!, (255 * backgroundAlpha).toInt())

        canvas?.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)

        val border = Paint()
        val verticalSpacing =  height * 0.2f
        val horizonralSpacing =  width * 0.1f

   /*     border.color = Color.parseColor("#FE3386")
        border.strokeWidth = 20f
        border.style = Paint.Style.STROKE
        border.isAntiAlias = true
        border.isDither = true
        border.pathEffect = DashPathEffect(floatArrayOf(45f, 45f), 0f)

        canvas?.drawRoundRect(horizonralSpacing, verticalSpacing,
            (width - horizonralSpacing), (height - verticalSpacing), 500f, 500f, border)
*/
        val holePaint = Paint()

        holePaint.color = ContextCompat.getColor(context, android.R.color.transparent)
        holePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        canvas?.drawRoundRect(horizonralSpacing, verticalSpacing,
            (width - horizonralSpacing), (height - verticalSpacing), 500f, 500f, holePaint)
    }

    private fun initProcessCanvas () {
        processBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        processCanvas = Canvas(processBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackgroundShape(canvas)
        synchronized(lock) {
            initProcessCanvas()
            graphics.forEach {
                it.draw(canvas)
                it.draw(processCanvas)
            }
        }
    }

}