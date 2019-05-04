package com.example.myapplication

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.show_img.*

class UploadAct : AppCompatActivity() {

    val PERMISSION_REQUEST_CODE = 1001
    val PICK_IMAGE_REQUEST = 900
    lateinit var filePath : Uri

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.show_img)
        showImage.setOnClickListener {
            when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) -> {
                    if (ContextCompat.checkSelfPermission(this@UploadAct, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE)
                    }else{
                        choseFile()
                    }
                }
                else -> choseFile()
            }
        }
    }
    private fun choseFile(){
        val intent = Intent().apply {
            type = "*/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent,
            "Select Picture"
            ),
            PICK_IMAGE_REQUEST
            )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this@UploadAct, "Opps! Permission Denied!!", Toast.LENGTH_SHORT).show()
                else
                    choseFile()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {0
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK){
            return
        }
        when (requestCode){
            PICK_IMAGE_REQUEST -> {
                filePath = data!!.getData()
                uploadFile()
            }
        }
    }

    private fun uploadFile() {
        val progress = ProgressDialog(this).apply {
            setTitle("Uploading Picture....")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
        val data = FirebaseStorage.getInstance()
        var value = 0.0
        var storage = data.getReference().child("mypic.jpg").putFile(filePath).addOnProgressListener {
            taskSnapshot ->
            value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            Log.v("value", "value==" + value)
            progress.setMessage("Upload.." + value.toInt() + "%")
        }
            .addOnSuccessListener { taskSnapshot -> progress.dismiss()
                val uri = taskSnapshot.storage.downloadUrl
                Log.v("Download File", "File.." +uri)
                Glide.with(this@UploadAct).load(uri).into(showImage)
            }.addOnFailureListener {
                exception -> exception
                .printStackTrace()
            }
    }

}