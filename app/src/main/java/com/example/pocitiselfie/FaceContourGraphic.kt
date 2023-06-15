package com.example.pocitiselfie

import android.graphics.*
import android.graphics.drawable.shapes.RoundRectShape
import android.util.Log
import androidx.annotation.ColorInt
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour


class FaceContourGraphic(
    private val overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint


    init {
        val selectedColor = Color.WHITE

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH


    }

    private fun Canvas.drawFace(facePosition: Int, @ColorInt selectedColor: Int) {
        val contour = face.getContour(facePosition)
        val path = Path()
        contour?.points?.forEachIndexed { index, pointF ->
            if (index == 0) {
                path.moveTo(
                    translateX(pointF.x),
                    translateY(pointF.y)
                )
            }
            path.lineTo(
                translateX(pointF.x),
                translateY(pointF.y)
            )
        }
        val paint = Paint().apply {
            color = selectedColor
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
        drawPath(path, paint)
    }

    override fun draw(canvas: Canvas?) {

        val faceBoundingBox = face.boundingBox

        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            faceBoundingBox
        )
//        canvas?.drawRect(rect, boxPaint)


        val border = Paint()
        val verticalSpacing =  overlay.height * 0.2f
        val horizonralSpacing =  overlay.width * 0.1f

        val rectBounding = RectF(horizonralSpacing, verticalSpacing,
            (overlay.width  - horizonralSpacing), (overlay.height - verticalSpacing))

        border.color = Color.parseColor("#FE3386")
        border.strokeWidth = 10f
        border.style = Paint.Style.STROKE
        border.isAntiAlias = true
        border.isDither = true

//        border.pathEffect = DashPathEffect(floatArrayOf(45f, 45f), 0f)

/*        if(
            faceBoundingBox.left > rectBounding.left &&
            faceBoundingBox.top > rectBounding.top &&
            faceBoundingBox.bottom < rectBounding.bottom &&
            faceBoundingBox.right < rectBounding.right
        ){

        }else{

        }*/




        if(!rectBounding.contains(rect)){
            border.pathEffect = DashPathEffect(floatArrayOf(45f, 45f), 0f)
        }

        canvas?.drawRoundRect(rectBounding, 500f, 500f, border)

//        canvas?.drawRect(rectBounding, boxPaint)

/*
        val contours = face.allContours

        contours.forEach {
            it.points.forEach { point ->
                val px = translateX(point.x)
                val py = translateY(point.y)
                canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
            }
        }*/

        // face
//        canvas?.drawFace(FaceContour.FACE, Color.BLUE)

        // left eye
/*        canvas?.drawFace(FaceContour.LEFT_EYEBROW_TOP, Color.RED)
        canvas?.drawFace(FaceContour.LEFT_EYE, Color.BLACK)
        canvas?.drawFace(FaceContour.LEFT_EYEBROW_BOTTOM, Color.CYAN)

        // right eye
        canvas?.drawFace(FaceContour.RIGHT_EYE, Color.DKGRAY)
        canvas?.drawFace(FaceContour.RIGHT_EYEBROW_BOTTOM, Color.GRAY)
        canvas?.drawFace(FaceContour.RIGHT_EYEBROW_TOP, Color.GREEN)

        // nose
        canvas?.drawFace(FaceContour.NOSE_BOTTOM, Color.LTGRAY)
        canvas?.drawFace(FaceContour.NOSE_BRIDGE, Color.MAGENTA)
*/

        val smile = face.smilingProbability

        if(smile != null && smile > 0.9) {
            canvas?.drawFace(FaceContour.LOWER_LIP_BOTTOM, Color.WHITE)
            canvas?.drawFace(FaceContour.LOWER_LIP_TOP, Color.YELLOW)
            canvas?.drawFace(FaceContour.UPPER_LIP_BOTTOM, Color.GREEN)
            canvas?.drawFace(FaceContour.UPPER_LIP_TOP, Color.CYAN)
        }

        val blinkLeft = face.leftEyeOpenProbability

        if(blinkLeft != null && blinkLeft < 0.1){
            canvas?.drawFace(FaceContour.RIGHT_EYE, Color.DKGRAY)
            canvas?.drawFace(FaceContour.RIGHT_EYEBROW_BOTTOM, Color.GRAY)
            canvas?.drawFace(FaceContour.RIGHT_EYEBROW_TOP, Color.GREEN)
        }


        Log.d("FaceContourGraphic", "${face.headEulerAngleX}")




        // rip

    }

    companion object {
        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}