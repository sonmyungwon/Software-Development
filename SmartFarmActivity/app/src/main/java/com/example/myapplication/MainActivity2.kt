package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class MainActivity2 : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val startBtn = findViewById<Button>(R.id.startBtn)
        startBtn.setOnClickListener{
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("message")

            myRef.push().setValue("Hello, World!")

        }

    }
}