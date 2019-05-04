package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.add_data.*
import java.io.IOException
import java.util.*

class UploadType2 : AppCompatActivity() {
    lateinit var btnChoose : Button
    lateinit var btnUpload : Button
    lateinit var imgView : ImageView

    val PICK_IMAGE_REQUEST = 71
    val PERMISSION_REQUEST_CODE = 1001
    var value = 0.0
    lateinit var filepath : Uri
    lateinit var storage : FirebaseStorage
    lateinit var storageReference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_img_type2)
        btnChoose = findViewById(R.id.btn_choose)
        btnUpload = findViewById(R.id.btn_upload)
        imgView = findViewById(R.id.imgView)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        btnChoose.setOnClickListener {
            when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) -> {
                    if (ContextCompat.checkSelfPermission(this@UploadType2, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE)
                    }else{
                        chooseImage()
                    }
                }
                else -> chooseImage()
            }
        }
        btnUpload.setOnClickListener {
            uploadImage()
        }
    }

    private fun uploadImage() {
        val progress = ProgressDialog(this).apply {
            setTitle("Uploading Picture....")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
        var ref : StorageReference = storageReference.child("images/" +UUID.randomUUID().toString())
        ref.putFile(filepath).addOnSuccessListener {
            taskSnapshot -> progress.dismiss()
            Toast.makeText(this@UploadType2, "Upload", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                e -> progress.dismiss()
                Toast.makeText(this@UploadType2, "Failed" +e.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener {
                taskSnapshot ->
                value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                progress.setMessage("Uploaded.." + value.toInt() + "%")
            }
    }

    private fun chooseImage() {
        val intent = Intent().apply {
            type = "*/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
            Intent.createChooser(intent,
                "Select Picture"
            ),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null){
            filepath = data.data
            try {
                var bitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,filepath)
                imgView.setImageBitmap(bitmap)

            }catch (e : IOException){
                e.printStackTrace()

            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this@UploadType2, "Opps! Permission Denied!!", Toast.LENGTH_SHORT).show()
                else
                    chooseImage()
            }
        }
    }
}