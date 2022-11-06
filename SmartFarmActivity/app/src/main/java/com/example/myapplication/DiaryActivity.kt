package com.example.myapplication

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.GregorianCalendar

class DiaryActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("user/diary/avg_led")

        val avgTemp = findViewById<TextView>(R.id.avgTemp)
        val avgLight = findViewById<TextView>(R.id.avgLight)
        val water = findViewById<TextView>(R.id.water)
        val growth = findViewById<TextView>(R.id.growth)

        val dateBtn = findViewById<ImageView>(R.id.dateBtn)
        val textDay = findViewById<TextView>(R.id.dayText)

        val today = GregorianCalendar()
        val year : Int = today.get(Calendar.YEAR)
        val month : Int = today.get(Calendar.MONTH)
        val date : Int = today.get(Calendar.DATE)

        dateBtn?.setOnClickListener{

            val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {

                override fun onDateSet(view: DatePicker?,year: Int, month: Int, dayOfMonth: Int)
                {
                    Log.d("MAIN", "${year}. ${month + 1}. $dayOfMonth")
                    textDay.text = "${year}. ${month + 1}. $dayOfMonth"
                }

            },year,month, date)

            dlg.show()
        }

        var imageUrl:String = ""

        val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://smartfarmactivity.appspot.com/text_bar.png")
        var storageRef = storage.reference
        val pathReference = storageRef.child("images/text_bar.png")

        fun loadImage(imageView: ImageView, url: String){
            pathReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(imageView.context)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(imageView)
            }
        }

    }
}