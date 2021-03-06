package ru.skillbranch.avatarimageview.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.animation.doOnRepeat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.skillbranch.avatarimageview.R
import ru.skillbranch.avatarimageview.extensions.dpToPix
import kotlin.math.max
import kotlin.math.truncate

class AvatarImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0
): androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    companion object{
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE

        private const val DEFAULT_SIZE = 40

         val bgColors = arrayOf(
            Color.parseColor("#7BC862"),
            Color.parseColor("#E17076"),
            Color.parseColor("#FAA774"),
            Color.parseColor("#6EC9CB"),
            Color.parseColor("#65AADD"),
            Color.parseColor("#A695E7"),
            Color.parseColor("#EE7AAE"),
            Color.parseColor("#2196E3")

        )
    }
    @Px
    var borderWidth: Float = context.dpToPix(DEFAULT_BORDER_WIDTH)
    @ColorInt
    private var borderColor: Int = Color.WHITE
    private var initials:String = "??"

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private val borderRect = Rect()
    private var size = 0

    private var isAvatarMode = true

    init {
        if(attrs!=null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageView)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageView_aiv_borderWidth,
                context.dpToPix(DEFAULT_BORDER_WIDTH)
            )
            borderColor = ta.getColor(R.styleable.AvatarImageView_aiv_borderColor,DEFAULT_BORDER_COLOR)
            initials = ta.getString(R.styleable.AvatarImageView_aiv_initials) ?: "??"
            ta.recycle()
        }
        scaleType = ScaleType.CENTER_CROP
        setup()
        setOnLongClickListener{
            handelLongClick()
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d("M_AvatarImageView","""
            onMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}""".trimIndent())

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(max(initSize,size), max(initSize,size))
        Log.d("M_AvatarImageView","onMeasure after set size $measuredWidth $measuredHeight")
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("M_AvatarImageView","onSizeChanged")
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
        Log.d("M_AvatarImageView","onDraw")
        //NOT allocate, ONLY draw
        if (drawable != null && isAvatarMode){
            drawAvatar(canvas)
        }else{
            drawInitials(canvas)
        }
        //resize rect
        val half = (borderWidth/2).toInt()
        borderRect.set(viewRect)
        borderRect.inset(half,half)
        canvas.drawOval(borderRect.toRectF(),borderPaint)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SaveState(super.onSaveInstanceState())
        savedState.isAvatarMode =isAvatarMode
        savedState.borderWidth = borderWidth
        savedState.borderColor = borderColor
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SaveState){
            super.onRestoreInstanceState(state)
            isAvatarMode = state.isAvatarMode
            borderWidth = state.borderWidth
            borderColor = state.borderColor

            with(borderPaint){
                color =borderColor
                strokeWidth = borderWidth
            }
        }else{
            super.onRestoreInstanceState(state)
        }

    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (isAvatarMode) prepareShader(width,height)
        Log.d("M_AvatarImageView","setImageBitmap")
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (isAvatarMode) prepareShader(width,height)
        Log.d("M_AvatarImageView","setImageDrawable")
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (isAvatarMode) prepareShader(width,height)
        Log.d("M_AvatarImageView","setImageResource")
    }

    fun setInitials(initials: String){
        this.initials = initials
        if(!isAvatarMode){
            invalidate()
        }
    }

    fun setBorderColor(@ColorInt color: Int){
        borderColor =color
        borderPaint.color = borderColor
        invalidate()
    }

    fun setBorderWidth(@Dimension width: Int){
        borderWidth = context.dpToPix(width)
        borderPaint.strokeWidth = borderWidth
        invalidate()
    }

    private fun setup() {


        with(borderPaint){
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }
    private fun prepareShader(w: Int, h: Int){
        //prepare buffer this
        if (w ==0 || drawable == null) return
        val srcBm = drawable.toBitmap(w,h, Bitmap.Config.ARGB_8888)
        avatarPaint.shader =  BitmapShader(srcBm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

    }

    private fun resolveDefaultSize(spec:Int):Int{
        return when (MeasureSpec.getMode(spec)){
            MeasureSpec.UNSPECIFIED -> context.dpToPix(DEFAULT_SIZE).toInt() //resolve default size
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec) // from spec
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec) // from spec
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun drawAvatar(canvas: Canvas){
        canvas.drawOval(viewRect.toRectF(),avatarPaint)
    }
    private fun drawInitials(canvas: Canvas){
        initialsPaint.color = initialsToColor(initials)
        canvas.drawOval(viewRect.toRectF(), initialsPaint)
        with(initialsPaint){
            color =Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = height * 0.33f
        }
        val offsetY = (initialsPaint.descent() + initialsPaint.ascent())/2
        canvas.drawText(initials,viewRect.exactCenterX(),viewRect.exactCenterY()-offsetY, initialsPaint)
    }

    private fun initialsToColor(letters:String):Int{
        val b = letters[0].toByte()
        val len = bgColors.size
        val d = b/len.toDouble()
        val index = ((d- truncate(d))*len).toInt()
        return bgColors[index]
    }
    private fun handelLongClick():Boolean{
        val va = ValueAnimator.ofInt(width,width*2).apply {
            duration = 300
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1
        }
        va.addUpdateListener {
            size = it.animatedValue as Int
            requestLayout()
        }
        va.doOnRepeat { toggleMode() }
        va.start()
        return true
    }
    private fun toggleMode(){
        isAvatarMode =!isAvatarMode
        invalidate()
    }

        private class SaveState : BaseSavedState, Parcelable{
            var isAvatarMode:Boolean = true
            var borderWidth:Float = 0f
            var borderColor:Int = 0

        constructor(superState: Parcelable?): super(superState)

        constructor(src: Parcel) : super(src) {
            //restore state from parcel
            isAvatarMode = src.readInt() == 1
            borderWidth = src.readFloat()
            borderColor = src.readInt()
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            //write state to parcel
            super.writeToParcel(dst, flags)
            dst.writeInt(if(isAvatarMode) 1 else 0)
            dst.writeFloat(borderWidth)
            dst.writeInt(borderColor)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SaveState> {
            override fun createFromParcel(parcel: Parcel) = SaveState(parcel)



            override fun newArray(size: Int): Array<SaveState?> = arrayOfNulls(size)


        }

    }
}