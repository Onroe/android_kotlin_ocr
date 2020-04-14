package com.example.ocrapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates
import org.jetbrains.anko.toast
import java.lang.StringBuilder

class CameraActivity : AppCompatActivity() {

    private var cameraSource by Delegates.notNull<CameraSource>()
    private  var textrecognize by Delegates.notNull<TextRecognizer>()

    private val CAMERA_PERMISSION =100
    private val TAG ="MobileVision"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadCamera()
        val detect_camera =  findViewById(R.id.button_id) as Button
        detect_camera.setOnClickListener{
            recognizeText()
        }
    }

    private fun loadCamera(){

        textrecognize = TextRecognizer.Builder(this).build()
        if(!textrecognize.isOperational){
            Log.w(TAG, "Dependency not yet loaded")
            return
        }

        cameraSource =  CameraSource.Builder(applicationContext,textrecognize)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280,1024)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()

           surface_camera_view.holder.addCallback(object : SurfaceHolder.Callback {
               override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

               }
               override fun surfaceDestroyed(p0: SurfaceHolder?) {
                   cameraSource.stop()
               }
               @SuppressLint("MissingPermission")
               override fun surfaceCreated(p0: SurfaceHolder?) {
                   try {
                       if (isCameraPermissionGranted()) {
                           cameraSource.start(surface_camera_view.holder)
                       } else {
                           requestForPermission()
                       }
                   } catch (e: Exception) {
                       toast("Error:  ${e.message}")
                   }
               }
           })


    }


    //CHECK IF CAMERA PERMISON IS GRANTED
    fun isCameraPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestForPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
    }

    //CHECK CAMERA PERMISSION
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != CAMERA_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isCameraPermissionGranted()) {
                cameraSource.start(surface_camera_view.holder)
            } else {
                toast("Camera needed")
                finish()
            }
        }
    }
     private fun recognizeText(){
         textrecognize.setProcessor(object: Detector.Processor<TextBlock>{
             override fun release() {}

             override fun receiveDetections(recognizedtext: Detector.Detections<TextBlock>) {
                 val textitems = recognizedtext.detectedItems

                 if(textitems.size() != 0){
                     text_result.post{
                         val stringBuilder = StringBuilder()
                         for (i in 0 until textitems.size()) {
                             val item = textitems.valueAt(i)
                             stringBuilder.append(item.value)
                             stringBuilder.append("\n")
                         }
                         text_result.text = stringBuilder
                     }

                 }
             }
         })
     }
}
