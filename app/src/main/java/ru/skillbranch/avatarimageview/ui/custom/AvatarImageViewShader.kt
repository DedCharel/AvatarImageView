package ru.skillbranch.avatarimageview.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.skillbranch.avatarimageview.R
import ru.skillbranch.avatarimageview.extensions.dpToPix

class AvatarImageViewShader@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0
): androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    companion object{
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE

        private const val DEFAULT_SIZE = 40
    }
    @Px
    var borderWidth: Float = context.dpToPix(DEFAULT_BORDER_WIDTH)
    @ColorInt
    private var borderColor: Int = Color.WHITE
    private var initials:String = "??"

    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()


    init {
        if(attrs!=null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageViewMask)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageViewShader_aivs_borderWidth,
                context.dpToPix(DEFAULT_BORDER_WIDTH)
            )
            borderColor = ta.getColor(R.styleable.AvatarImageViewShader_aivs_borderColor,DEFAULT_BORDER_COLOR)
            initials = ta.getString(R.styleable.AvatarImageViewShader_aivs_initials) ?: "??"
            ta.recycle()
        }
        scaleType = ScaleType.CENTER_CROP
        setup()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d("M_AvatarImageViewShader","""
            onMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}""".trimIndent())

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
        Log.d("M_AvatarImageViewShader","onMeasure after set size $measuredWidth $measuredHeight")
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("M_AvatarImageViewShader","onSizeChanged")
        if (w==0) return
        with(viewRect){
            left = 0
            top = 0
            right = w
            bottom = h
        }
        prepareShader(w,h)
    }


    override fun onDraw(canvas: Canvas) {
        //   super.onDraw(canvas)
        Log.d("M_AvatarImageViewShader","onDraw")
        //NOT allocate, ONLY draw
        canvas.drawOval(viewRect.toRectF(),avatarPaint)
        //resize rect
        val half = (borderWidth/2).toInt()
        viewRect.inset(half,half)
        canvas.drawOval(viewRect.toRectF(),borderPaint)
    }

    private fun setup() {
        with(borderPaint){
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }
    private fun prepareShader(w: Int, h: Int){
       val   srcBm = drawable.toBitmap(w,h, Bitmap.Config.ARGB_8888)
        avatarPaint.shader = BitmapShader(srcBm,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP)

    }

    private fun resolveDefaultSize(spec:Int):Int{
        return when (MeasureSpec.getMode(spec)){
            MeasureSpec.UNSPECIFIED -> context.dpToPix(DEFAULT_SIZE).toInt() //resolve default size
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec) // from spec
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec) // from spec
            else -> MeasureSpec.getSize(spec)
        }
    }
}