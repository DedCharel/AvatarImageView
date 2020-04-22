package ru.skillbranch.avatarimageview.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.skillbranch.avatarimageview.R
import ru.skillbranch.avatarimageview.extensions.dpToPix

class AvatarImageViewMask @JvmOverloads constructor(
    context: Context,
    attrs:AttributeSet? = null,
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

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private lateinit var resultBm:Bitmap
    private lateinit var maskBm:Bitmap
    private lateinit var srcBm:Bitmap

    init {
        if(attrs!=null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageViewMask)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageViewMask_aivm_borderWidth,
                context.dpToPix(DEFAULT_BORDER_WIDTH)
            )
            borderColor = ta.getColor(R.styleable.AvatarImageViewMask_aivm_borderColor,DEFAULT_BORDER_COLOR)
            initials = ta.getString(R.styleable.AvatarImageViewMask_aivm_initials) ?: "??"
            ta.recycle()
        }
        scaleType = ScaleType.CENTER_CROP
        setup()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d("M_AvatarImageViewMask","""
            onMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}""".trimIndent())

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
        Log.d("M_AvatarImageViewMask","onMeasure after set size $measuredWidth $measuredHeight")
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("M_AvatarImageViewMask","onSizeChanged")
        if (w==0) return
        with(viewRect){
            left = 0
            top = 0
            right = w
            bottom = h
        }
        prepareBitmaps(w,h)
    }


    override fun onDraw(canvas: Canvas) {
     //   super.onDraw(canvas)
        Log.d("M_AvatarImageViewMask","onDraw")
        //NOT allocate, ONLY draw
        canvas.drawBitmap(resultBm,viewRect,viewRect,null)
        //resize rect
        val half = (borderWidth/2).toInt()
        viewRect.inset(half,half)
        canvas.drawOval(viewRect.toRectF(),borderPaint)
    }

    private fun setup() {
        with(maskPaint) {
             color = Color.RED
            style = Paint.Style.FILL
        }

        with(borderPaint){
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }
    private fun prepareBitmaps(w: Int, h: Int){
        //prepare buffer this
        maskBm = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        resultBm = maskBm.copy(Bitmap.Config.ARGB_8888, true)
        val maskCanvas = Canvas(maskBm)
        maskCanvas.drawOval(viewRect.toRectF(), maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        srcBm = drawable.toBitmap(w,h,Bitmap.Config.ARGB_8888)

        val resultCanvas = Canvas(resultBm)

        resultCanvas.drawBitmap(maskBm, viewRect, viewRect, null)
        resultCanvas.drawBitmap(srcBm, viewRect, viewRect, maskPaint)

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
