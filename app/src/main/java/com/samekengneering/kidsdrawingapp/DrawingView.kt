package com.samekengneering.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context:Context,attrs:AttributeSet):View(context,attrs) {
    private var mDrawPath:CustomPath?=null
    private var mCanvasBitMap:Bitmap?=null
    private var mDrawingPaint: Paint?=null
    private var mCanvasPaint:Paint?=null
    private var color= Color.BLACK
    private var mBrushSize:Float=0.toFloat()
    private var canvas:Canvas?=null
    private val paths=ArrayList<CustomPath>()
    private val mUndoPaths=ArrayList<CustomPath>()
    init {
        setUpDrawing()
    }
    fun onClickUndo(){
        if(paths.size>0){
            mUndoPaths.add(paths.removeAt(paths.size-1))
            invalidate()
        }
    }
    private fun setUpDrawing(){
        mDrawPath=CustomPath(color,mBrushSize)
        mDrawingPaint= Paint()
        mDrawingPaint!!.color=color
        mDrawingPaint!!.style=Paint.Style.STROKE
        mDrawingPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawingPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint=Paint(Paint.DITHER_FLAG)
        //mBrushSize=20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitMap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(mCanvasBitMap!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var touchx=event?.x
        var touchy=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color=color
                mDrawPath!!.brushThickness=mBrushSize

                mDrawPath!!.reset()
                if (touchx != null) {
                    if (touchy != null) {
                        mDrawPath!!.moveTo(touchx,touchy)
                    }
                }
            }
            MotionEvent.ACTION_MOVE->{
                if (touchx != null) {
                    if (touchy != null) {
                        mDrawPath!!.lineTo(touchx,touchy)
                    }
                }
            }
            MotionEvent.ACTION_UP->
            {
                paths.add(mDrawPath!!)
                mDrawPath=CustomPath(color,mBrushSize)
            } else-> return false
        }
        invalidate()
        return true
    }
    fun setColor(newColor:String){
        color=Color.parseColor(newColor)
        mDrawingPaint!!.color=color
    }
    fun setSizeforBrush(newSize:Float){
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawingPaint!!.strokeWidth=mBrushSize
    }
    override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitMap!!,0f,0f,mCanvasPaint)
        for ( path in paths){
            mDrawingPaint!!.strokeWidth=path!!.brushThickness
            mDrawingPaint!!.color=path!!.color
            canvas.drawPath(path,mDrawingPaint!!)
        }
        if(!mDrawPath!!.isEmpty){
            mDrawingPaint!!.strokeWidth=mDrawPath!!.brushThickness
            mDrawingPaint!!.color=mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawingPaint!!)
        }
    }

    internal inner class CustomPath(var color:Int,var brushThickness:Float): Path()
}