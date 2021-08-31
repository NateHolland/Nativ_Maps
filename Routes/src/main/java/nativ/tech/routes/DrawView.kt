package nativ.tech.routes

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class DrawView: AppCompatImageView {
    var dashedLine:Circle? = null
    private var newPoint:Circle? = null
    var showCentre: Boolean = java.lang.Boolean.TRUE

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(showCentre)dashedLine?.let { circle ->
            canvas?.let { it ->
                val mPath: Path = Path()
                mPath.moveTo(circle.x,circle.y)
                newPoint?.let { p ->
                    mPath.lineTo(p.x,p.y)
                    val mPaint = Paint()
                    mPaint.setARGB(255, 0, 0, 0)
                    mPaint.style = Paint.Style.STROKE
                    mPaint.strokeWidth = 10f
                    mPaint.pathEffect = DashPathEffect(floatArrayOf(5f, 10f, 15f, 20f), 0F)
                    it.drawPath(mPath,mPaint)
                }

            }
        }
        if(showCentre)newPoint?.let { circle ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.RED
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 10f
            canvas?.let { it.drawCircle(circle.x,circle.y,circle.r,paint) }
        }?:run{
            newPoint = Circle(width/2f,height/2f,30f)
            invalidate()
        }

    }
}