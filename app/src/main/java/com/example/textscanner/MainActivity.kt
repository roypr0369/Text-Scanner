package com.example.textscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var btnCapture: Button
    private lateinit var btnCopy: Button
    private lateinit var tvData: TextView
    private val REQUEST_CAMERA_CODE = 100
    private lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnCapture = findViewById(R.id.button_capture)
        btnCopy = findViewById(R.id.button_copy)
        tvData = findViewById(R.id.textData)

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),  REQUEST_CAMERA_CODE)
        }
        btnCapture.setOnClickListener {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this)

        }
        btnCopy.setOnClickListener {
            val scannedText = tvData.text.toString()
            copyToClipBoard(scannedText)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                val resultUri = result.uri
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    getTextFromImage(bitmap)
                }catch (e : IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getTextFromImage(bitmap: Bitmap){
        val recognizer = TextRecognizer.Builder(this).build()
        if(!recognizer.isOperational){
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
        }
        else{
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val textBlockSparseArray = recognizer.detect(frame)
            val stringBuilder = StringBuilder()
            for(i in 0 until textBlockSparseArray.size()){
                val textBlock = textBlockSparseArray.valueAt(i)
                stringBuilder.append(textBlock.value)
                stringBuilder.append("\n")
            }
            tvData.text = stringBuilder.toString()
            btnCapture.text = getString(R.string.retake)
            btnCopy.visibility = View.VISIBLE

        }
    }

    private fun copyToClipBoard(text: String){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied data", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this,"Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}