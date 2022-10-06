package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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
            val myRef = database.getReference("/manual/sensor/pump")

            myRef.setValue(1)

        }
        val startBtn2 = findViewById<Button>(R.id.startBtn2)
        startBtn2.setOnClickListener{
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("/manual/sensor/pump")

            myRef.setValue(0)

        }
        val startBtn3 = findViewById<Button>(R.id.startBtn3)
        startBtn3.setOnClickListener{
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("/manual/sensor/fan")

            myRef.setValue(1)

        }
        val startBtn4 = findViewById<Button>(R.id.startBtn4)
        startBtn4.setOnClickListener{
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("/manual/sensor/fan")

            myRef.setValue(0)

        }
        val autoControlBtn = findViewById<Button>(R.id.autoControlBtn)
        autoControlBtn.setOnClickListener(){
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.auto_control_custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("자동 제어 설정")

            val mAlertDialog = mBuilder.show()
            mAlertDialog.findViewById<Button>(R.id.button2)?.setOnClickListener(){
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("/manual/sensor/fan")

                myRef.setValue(1)
            }
            mAlertDialog.findViewById<Button>(R.id.button3)?.setOnClickListener(){
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("/manual/sensor/fan")

                myRef.setValue(0)
            }


        }


    }
}