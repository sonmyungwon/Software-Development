package com.example.imageupload

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.imageupload.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage.*
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.getImage.setOnClickListener(){

            val imageName = binding.etImageId.text.toString()
            val storageRef = getInstance().reference.child("images/$imageName.png")
            val localFile = File.createTempFile("tempImage", "png")

            storageRef.getFile(localFile).addOnSuccessListener {

                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.imageView.setImageBitmap(bitmap)

            }.addOnFailureListener{

                Toast.makeText(this, "Failed ", Toast.LENGTH_SHORT).show()
            }

        }

    }
}