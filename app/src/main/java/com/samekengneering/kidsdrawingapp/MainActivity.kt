package com.samekengneering.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brush_selector_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_custom.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private inner class ExecuteAsyncTask(val value:String): AsyncTask<Any, Void, String>(){
        var customProgressDialog:Dialog?=null
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        private fun showProgressDialog() {
            customProgressDialog= Dialog(this@MainActivity)
            customProgressDialog!!.setContentView(R.layout.progress_custom_layout)
            customProgressDialog!!.show()
        }

        override fun doInBackground(vararg params: Any?): String {
            for ( i in 1..10000){
                Log.e("i: ",""+i)
            }
            return value
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
        }

        private fun cancelProgressDialog() {


            customProgressDialog!!.dismiss()
        }
    }
    private inner class BitMapAsyncTask(val mBitMap:Bitmap):AsyncTask<Any,Void,String>(){

        override fun doInBackground(vararg params: Any?): String {
            var result=""
            if(mBitMap!=null){
                try {
                    val bytes=ByteArrayOutputStream()
                   mBitMap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    var f=File(externalCacheDir!!.absoluteFile.toString(),"KidsDrawingApp_"+File.separator+System.currentTimeMillis()/1000+".png")
                    var fos=FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result=f.absolutePath
                }catch (e:Exception){
                    result=""
e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result:String?) {
            super.onPostExecute(result)
            if(!result!!.isEmpty()){
            Toast.makeText(this@MainActivity,"Image Extraction is done",Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this@MainActivity,"Something is wrong",Toast.LENGTH_LONG).show()
            }
        }


    }
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

            if(isReadStorageAllowed() && ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"You already have a permission",Toast.LENGTH_SHORT).show()
                // run our code to get our image from gallery and camera
                var pickAnImage=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickAnImage, GALLERY)
            } else {
                //Request Permission
                requestStoragePermission()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        CAMERA_ACCESS_CODE)
                Toast.makeText(this,"You don't have a permission",Toast.LENGTH_SHORT).show()
            }


        }
        ib_undo.setOnClickListener {

            drawing_view.onClickUndo()
            ExecuteAsyncTask("Background task is running").execute()
        }
        alertdialog.setOnClickListener{view->
            customDialog(view)
        }
        ib_save.setOnClickListener {
            if(isWriteStorageAllowed()){
                BitMapAsyncTask(getBitMapFromView(fl_drawing_view))
            }else{
                requestStoragePermission()
            }
            
        }
    }
    //method after a request for permission we access the request result by overriding the default method
  fun requestStoragePermission(){
      if(ActivityCompat.shouldShowRequestPermissionRationale(this,
              arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
          Toast.makeText(this,"Need to have a storage permission", Toast.LENGTH_LONG).show()
      }
        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_ACCESS_CODE)
  }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode== GALLERY){
                try {
                    if(data!!.data!=null){
                    iv_background.visibility=View.VISIBLE
                        iv_background.setImageURI(data.data)

                    }else{
                        Toast.makeText(this,"Error Parsing the Image or its corrupted",Toast.LENGTH_SHORT).show()
                    }
                } catch (ex:Exception){
                    ex.printStackTrace()
                }
            }
        }else{
            Toast.makeText(this,"You couldn't open the gallery",Toast.LENGTH_SHORT).show()
        }
    }
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
        }else if(requestCode== STORAGE_ACCESS_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"You grant an External Storage permission",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getBitMapFromView(view:View):Bitmap{
        val returnBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(returnBitmap)
        val bgdrawable=view.background
        if(bgdrawable!=null){
            bgdrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap
    }
    fun isWriteStorageAllowed():Boolean{
        var result=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return  result==PackageManager.PERMISSION_GRANTED
    }
    fun isReadStorageAllowed():Boolean{
        var result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return  result==PackageManager.PERMISSION_GRANTED
    }
    private fun customDialog(view: View){
        var dialog=Dialog(this)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.tv_submit.setOnClickListener(View.OnClickListener {
            Snackbar.make(view,"Submit is clicked from the dialog",Snackbar.LENGTH_SHORT).show()

            dialog.dismiss()

            customProgressBar()
        })
        dialog.tv_cancel.setOnClickListener(View.OnClickListener {
            Snackbar.make(view,"Cancel is clicked from the dialog",Snackbar.LENGTH_SHORT).show()
            dialog.dismiss()
        })
        dialog.show()
    }
    fun customProgressBar(){
        var customProgress=Dialog(this)
        customProgress.setContentView(R.layout.progress_custom_layout)

        customProgress.show()
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
        private const val STORAGE_ACCESS_CODE=2
        private const val GALLERY=2
    }
}