package com.samekengneering.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brush_selector_dialog_layout.*

class MainActivity : AppCompatActivity() {
    private var mImageButtonCurrentPaint:ImageButton?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       drawing_view.setSizeforBrush(20.toFloat())
        mImageButtonCurrentPaint=paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.palette_selected))
        ib_brush.setOnClickListener {
            showbrushChooserDialog(this)
        }
        ib_gallery.setOnClickListener { view->
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"You already have a permission",Toast.LENGTH_SHORT).show()
            } else {
                //Request Permission
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        CAMERA_ACCESS_CODE)
                Toast.makeText(this,"You don't have a permission",Toast.LENGTH_SHORT).show()
            }

        }
    }
    //method after a request for permission we access the request result by overriding the default method

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== CAMERA_ACCESS_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"You grant a permission",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"You deny a permission for the camera",Toast.LENGTH_LONG).show()
            }
        }
    }
    fun paintClicked(view:View){
        if(view!==mImageButtonCurrentPaint){
            val imagebutton=view as ImageButton
            var colortag=imagebutton.tag.toString()
            drawing_view.setColor(colortag!!)
            imagebutton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.palette_selected))
            mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.palette_normal))
            mImageButtonCurrentPaint=view

        }
    }
    private fun showbrushChooserDialog(context: Context){
        val brushDialog= Dialog(context)
        brushDialog.setContentView(R.layout.brush_selector_dialog_layout)
        brushDialog.setTitle("Brush Selector ")

        var small=brushDialog.image_small
        small.setOnClickListener {
            drawing_view.setSizeforBrush(10.toFloat())
            brushDialog.dismiss()
        }
        var medium=brushDialog.image_medium
        var large=brushDialog.image_large
        medium.setOnClickListener {
            drawing_view.setSizeforBrush(20.toFloat())
            brushDialog.dismiss()
        }
        large.setOnClickListener {
            drawing_view.setSizeforBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
    companion object{
        private const val CAMERA_ACCESS_CODE=1
    }
}