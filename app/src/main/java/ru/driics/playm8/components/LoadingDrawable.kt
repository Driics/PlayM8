package ru.driics.playm8.components

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.SystemClock
import ru.driics.playm8.core.utils.ViewUtils.toPx
import ru.driics.playm8.core.utils.animations.CubicBezierInterpolator
import kotlin.math.pow
import kotlin.math.roundToInt


class LoadingDrawable : Drawable {

    private val APPEAR_DURATION = 550f
    private val DISAPPEAR_DURATION = 320f

    private val rectTmp = RectF()

    private var start = -1L
    private var disappearStart = -1L
    private val matrix = Matrix()
    private val strokeMatrix = Matrix()
    private var gradient: LinearGradient? = null
    private var strokeGradient: LinearGradient? = null
    private var gradientColor1 = 0
    private var gradientColor2 = 0
    private var gradientStrokeColor1 = 0
    private var gradientStrokeColor2 = 0
    var stroke = false
    var backgroundColor: Int? = null
    var color1: Int = 0x262626
    var color2: Int =  0xf0f0f0
    var strokeColor1: Int? = null
    var strokeColor2: Int? = null
    private var gradientWidth = 0f
    private var gradientWidthScale = 1f
    private var speed = 1f
    var backgroundPaint = Paint().apply { isAntiAlias = true }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var usePath: Path? = null
    private val path = Path()
    private var lastBounds: Rect? = null
    private val radii = FloatArray(8)
    private val rectF = RectF()
    private var appearByGradient = false
    private var appearGradientWidth = 0
    private var appearPaint: Paint? = null
    private var appearGradient: LinearGradient? = null
    private var appearMatrix: Matrix? = null
    private var disappearGradientWidth = 0
    private var disappearPaint: Paint? = null
    private var disappearGradient: LinearGradient? = null
    private var disappearMatrix: Matrix? = null

    constructor(color1: Int, color2: Int) : this() {
        this.color1 = color1
        this.color2 = color2
    }

    constructor() {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 2f
    }

    fun setColor(color1: Int, color2: Int) {
        this.color1 = color1
        this.color2 = color2
        stroke = false
    }

    fun setColors(color1: Int, color2: Int, strokeColor1: Int, strokeColor2: Int) {
        this.color1 = color1
        this.color2 = color2
        stroke = true
        this.strokeColor1 = strokeColor1
        this.strokeColor2 = strokeColor2
    }

    fun setBackgroundColor(backgroundColor: Int) {
        backgroundPaint.color = backgroundColor.also { this.backgroundColor = it }
    }

    private fun isDisappearing(): Boolean {
        return disappearStart > 0 && SystemClock.elapsedRealtime() - disappearStart < DISAPPEAR_DURATION
    }

    private fun isDisappeared(): Boolean {
        return disappearStart > 0 && SystemClock.elapsedRealtime() - disappearStart >= DISAPPEAR_DURATION
    }

    fun timeToDisappear(): Long {
        return if (disappearStart > 0) {
            DISAPPEAR_DURATION.toLong() - (SystemClock.elapsedRealtime() - disappearStart)
        } else 0
    }

    /**
     * Устанавливает путь, используемый для рисования кисти.
     */
    fun usePath(path: Path) {
        usePath = path
    }

    /**
     * Устанавливает масштаб градиента.
     */
    fun setGradientScale(scale: Float) {
        gradientWidthScale = scale
    }

    /**
     * Устанавливает скорость рисования кисти.
     */
    fun setSpeed(speed: Float) {
        this.speed = speed
    }

    /**
     * Устанавливает, используется ли появление по градиенту.
     */
    fun setAppearByGradient(enabled: Boolean) {
        appearByGradient = enabled
    }

    /**
     * Устанавливает радиус скругления углов для всех углов в dp.
     *
     * Если используется путь рисования, эффект применяется к нему.
     */
    fun setRadiiDp(allDp: Float) {
        if (usePath != null) {
            paint.pathEffect = CornerPathEffect(allDp.toPx)
            strokePaint.pathEffect = CornerPathEffect(allDp.toPx)
        } else {
            setRadii(
                allDp.toPx,
                allDp.toPx,
                allDp.toPx,
                allDp.toPx
            )
        }
    }

    /**
     * Устанавливает радиус скругления углов для каждого угла в dp.
     */
    fun setRadiiDp(topLeftDp: Float, topRightDp: Float, bottomRightDp: Float, bottomLeftDp: Float) {
        setRadii(
            topLeftDp.toPx,
            topRightDp.toPx,
            bottomRightDp.toPx,
            bottomLeftDp.toPx
        )
    }

    fun setRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        val changed =
            radii[0] != topLeft
                    || radii[2] != topRight
                    || radii[4] != bottomRight
                    || radii[6] != bottomLeft

        radii[1] = topLeft
        radii[0] = radii[1]
        radii[3] = topRight
        radii[2] = radii[3]
        radii[5] = bottomRight
        radii[4] = radii[5]
        radii[7] = bottomLeft
        radii[6] = radii[7]

        if (lastBounds != null && changed) {
            path.rewind()
            rectF.set(lastBounds!!)
            path.addRoundRect(rectF, radii, Path.Direction.CW)
        }
    }

    /**
     * Устанавливает радиус скругления углов с помощью массива значений.
     *
     * @param radii массив значений, содержащий 8 элементов для каждого угла (в порядке верхнего левого, верхнего правого, нижнего правого, нижнего левого)
     */
    fun setRadii(radii: FloatArray) {
        if (radii.size != 8) {
            return
        }
        var changed = false
        for (i in 0..7) {
            if (this.radii[i] != radii[i]) {
                this.radii[i] = radii[i]
                changed = true
            }
        }
        if (lastBounds != null && changed) {
            path.rewind()
            rectF.set(lastBounds!!)
            path.addRoundRect(rectF, radii, Path.Direction.CW)
        }
    }

    /**
     * Устанавливает границы кисти.
     *
     * @param bounds прямоугольник, представляющий границы кисти
     */
    fun setBounds(bounds: RectF) {
        super.setBounds(
            bounds.left.toInt(),
            bounds.top.toInt(),
            bounds.right.toInt(),
            bounds.bottom.toInt()
        )
    }

    /**
     * Сбрасывает время начала анимации кисти.
     */
    fun reset() {
        start = -1
    }

    /**
     * Инициирует процесс исчезновения кисти, если она еще не исчезла или не находится в процессе исчезновения.
     */
    fun disappear() {
        if (!isDisappeared() && !isDisappearing()) {
            disappearStart = SystemClock.elapsedRealtime()
        }
    }

    /**
     * Сбрасывает время начала процесса исчезновения кисти.
     */
    fun resetDisappear() {
        disappearStart = -1
    }

    private fun getPaintAlpha(): Int {
        return paint.alpha
    }

    override fun setAlpha(i: Int) {
        paint.alpha = i
        if (i > 0) {
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSPARENT

    override fun draw(canvas: Canvas) {
        if (isDisappeared())
            return

        if (getPaintAlpha() <= 0)
            return

        var width = bounds.width()
        if (width <= 0)
            width = 200.toPx.roundToInt()

        val gwidth = (400.toPx.roundToInt().coerceAtMost(width) * gradientWidthScale)

        val strokeColor1 = if (this.strokeColor1 != null) strokeColor1 else color1
        val strokeColor2 = if (this.strokeColor2 != null) strokeColor1 else color2

        if (gradient == null || gwidth != gradientWidth || color1 != gradientColor1 || color2 != gradientColor2 || strokeColor1 != gradientStrokeColor1 || strokeColor2 != gradientStrokeColor2) {
            gradientWidth = gwidth

            gradientColor1 = color1!!
            gradientColor2 = color2!!

            gradient = LinearGradient(
                0f, 0f, gradientWidth, 0f,
                intArrayOf(gradientColor1, gradientColor2, gradientColor1),
                floatArrayOf(0f, .67f, 1f),
                Shader.TileMode.REPEAT
            ).apply {
                setLocalMatrix(matrix)
            }

            paint.shader = gradient

            gradientStrokeColor1 = strokeColor1!!
            gradientStrokeColor2 = strokeColor2!!

            strokeGradient = LinearGradient(
                0f,
                0f,
                gradientWidth,
                0f,
                intArrayOf(
                    gradientStrokeColor1,
                    gradientStrokeColor1,
                    gradientStrokeColor2,
                    gradientStrokeColor1
                ),
                floatArrayOf(0f, .4f, .67f, 1f),
                Shader.TileMode.REPEAT
            ).apply {
                setLocalMatrix(strokeMatrix)
            }

            strokePaint.shader = strokeGradient
        }

        val now = SystemClock.elapsedRealtime()
        if (start < 0)
            start = now

        var t = (now - start) / 2000f
        t = (t * speed / 4).toDouble().pow(.85).toFloat() * 4f

        val offset: Float = t * /*AndroidUtilities.density*/ 2 * gradientWidth % gradientWidth
        val appearT = (now - start) / APPEAR_DURATION
        val disappearT: Float =
            if (disappearStart > 0) 1f - CubicBezierInterpolator.EASE_OUT.getInterpolation(
                1f.coerceAtMost((now - disappearStart) / DISAPPEAR_DURATION)
            ) else 0f

        var disappearRestore = false
        if (isDisappearing()) {
            val disappearGradientWidthNow = 200.toPx.roundToInt().coerceAtLeast(bounds.width() / 3)

            if (disappearT < 1) {
                if (disappearPaint == null) {
                    disappearPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    disappearGradient = LinearGradient(
                        0f,
                        0f,
                        disappearGradientWidthNow.also {
                            disappearGradientWidth = it
                        }.toFloat(),
                        0f,
                        intArrayOf(-0x1, 0x00ffffff),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    disappearMatrix = Matrix()

                    disappearGradient!!.setLocalMatrix(disappearMatrix)
                    disappearPaint!!.apply {
                        shader = disappearGradient
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                    }
                } else if (disappearGradientWidth != disappearGradientWidthNow) {
                    disappearGradient = LinearGradient(
                        0f,
                        0f,
                        disappearGradientWidthNow.also {
                            disappearGradientWidth = it
                        }.toFloat(),
                        0f,
                        intArrayOf(-0x1, 0x00ffffff),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    disappearGradient!!.setLocalMatrix(disappearMatrix)
                    disappearPaint!!.shader = disappearGradient
                }

                rectF.set(bounds)
                rectF.inset(-strokePaint.strokeWidth, -strokePaint.strokeWidth)
                canvas.saveLayerAlpha(rectF, 255, Canvas.ALL_SAVE_FLAG)
                disappearRestore = true
            }
        }

        var appearRestore = false
        if (appearByGradient) {
            val appearGradientWidthNow = 200.toPx.roundToInt().coerceAtLeast(bounds.width() / 3)

            if (appearT < 1) {
                if (appearPaint == null) {
                    appearPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    appearGradient = LinearGradient(
                        0f,
                        0f,
                        appearGradientWidthNow.also {
                            appearGradientWidth = it
                        }.toFloat(),
                        0f,
                        intArrayOf(0x00ffffff, -0x1),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    appearMatrix = Matrix()
                    appearGradient!!.setLocalMatrix(appearMatrix)
                    appearPaint!!.shader = appearGradient
                    appearPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                } else if (appearGradientWidth != appearGradientWidthNow) {
                    appearGradient = LinearGradient(
                        0f,
                        0f,
                        appearGradientWidthNow.also {
                            appearGradientWidth = it
                        }.toFloat(),
                        0f,
                        intArrayOf(0x00ffffff, -0x1),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    appearGradient!!.setLocalMatrix(appearMatrix)
                    appearPaint!!.shader = appearGradient
                }

                rectF.set(bounds)
                rectF.inset(-strokePaint.strokeWidth, -strokePaint.strokeWidth)
                canvas.saveLayerAlpha(rectF, 255, Canvas.ALL_SAVE_FLAG)
                appearRestore = true
            }
        }

        matrix.setTranslate(offset, 0f)
        gradient!!.setLocalMatrix(matrix)

        strokeMatrix.setTranslate(offset, 0f)
        strokeGradient!!.setLocalMatrix(strokeMatrix)

        var drawPath: Path
        if (usePath != null) {
            drawPath = usePath!!
        } else {
            if (lastBounds == null || lastBounds!! != bounds) {
                path.rewind()

                lastBounds = bounds
                rectF.set(lastBounds!!)

                path.addRoundRect(rectF, radii, Path.Direction.CW)
            }

            drawPath = path
        }

        if (backgroundPaint != null) {
            canvas.drawPath(drawPath, backgroundPaint)
        }

        canvas.drawPath(drawPath, paint)

        if (stroke) {
            canvas.drawPath(drawPath, strokePaint)
        }

        if (appearRestore) {
            canvas.save()

            val appearOffset =
                appearT * (appearGradientWidth + bounds.width() + appearGradientWidth) - appearGradientWidth
            appearMatrix!!.setTranslate(bounds.left + appearOffset, 0f)
            appearGradient!!.setLocalMatrix(appearMatrix)

            val inset = strokePaint.strokeWidth.toInt()
            canvas.drawRect(
                (bounds.left - inset).toFloat(),
                (bounds.top - inset).toFloat(),
                (bounds.right + inset).toFloat(), (bounds.bottom + inset).toFloat(), appearPaint!!
            )
            canvas.restore()
            canvas.restore()
        }

        if (disappearRestore) {
            canvas.save()
            val appearOffset =
                disappearT * (disappearGradientWidth + bounds.width() + disappearGradientWidth) - disappearGradientWidth
            disappearMatrix!!.setTranslate(bounds.right - appearOffset, 0f)
            disappearGradient!!.setLocalMatrix(disappearMatrix)
            val inset = strokePaint.strokeWidth.toInt()
            canvas.drawRect(
                (bounds.left - inset).toFloat(),
                (bounds.top - inset).toFloat(),
                (bounds.right + inset).toFloat(),
                (bounds.bottom + inset).toFloat(),
                disappearPaint!!
            )
            canvas.restore()
            canvas.restore()
        }

        if (!isDisappeared()) {
            invalidateSelf()
        }
    }

    fun updateBounds() {
        if (usePath != null) {
            usePath!!.computeBounds(rectTmp, false)
            setBounds(rectTmp)
        }
    }
}