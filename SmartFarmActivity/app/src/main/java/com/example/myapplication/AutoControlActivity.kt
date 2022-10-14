package com.example.myapplication

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class AutoControlActivity : AppCompatActivity() {

    fun setValue(){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_control)

        val tempView : TextView = findViewById<TextView>(R.id.tempText)
        val tempPlusBtn : Button = findViewById<Button>(R.id.temp_plus_btn)
        val tempMinusBtn = findViewById<Button>(R.id.temp_minus_btn)

        val humidView : TextView = findViewById<TextView>(R.id.humidText)
        val humidPlusBtn  = findViewById<Button>(R.id.humid_plus_btn)
        val humidMinusBtn = findViewById<Button>(R.id.humid_minus_btn)

        val illumView : TextView = findViewById<TextView>(R.id.illumText)
        val illumPlusBtn  = findViewById<Button>(R.id.illum_plus_btn)
        val illumMinusBtn = findViewById<Button>(R.id.illum_minus_btn)

        val saveBtn = findViewById<Button>(R.id.save_btn)

        var tempNumber: Int = tempView.text.toString().toInt()

        tempMinusBtn.setOnClickListener{
            tempNumber-=5
            tempView.setText("$tempNumber")
//            num.setText(num1.text)
        }
        tempPlusBtn.setOnClickListener {
            tempNumber+=5
            tempView.setText("$tempNumber")
        }

        var humidNumber: Int = humidView.text.toString().toInt()

        humidMinusBtn.setOnClickListener{
            humidNumber-=5
            humidView.setText("$humidNumber")
//            num.setText(num1.text)
        }
        humidPlusBtn.setOnClickListener {
            humidNumber+=5
            humidView.setText("$humidNumber")
        }

        var illumNumber: Int = illumView.text.toString().toInt()

        illumMinusBtn.setOnClickListener{
            illumNumber-=5
            illumView.setText("$illumNumber")
//            num.setText(num1.text)
        }
        illumPlusBtn.setOnClickListener {
            illumNumber+=5
            illumView.setText("$illumNumber")
        }

        saveBtn.setOnClickListener(){

            val database = Firebase.database
            val myRef1 = database.getReference("user/auto/userdata/temp")
            val myRef2 = database.getReference("user/auto/userdata/soil_humi")
            val myRef3 = database.getReference("user/auto/userdata/light")
            val myRef4 = database.getReference("user/mode")

            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("알림")
                .setMessage("Are you sure? \n" +
                            "                Temperature: $tempNumber\n" +
                            "                Soil Humidity : $humidNumber\n" +
                            "                Lightness : $illumNumber")
                .setCancelable(false)

                .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        myRef1.setValue(tempNumber)
                        myRef2.setValue(humidNumber)
                        myRef3.setValue(illumNumber)
                        myRef4.setValue(2)
                        Toast.makeText(baseContext, "확인 버튼 클릭!", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton("취소", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        Toast.makeText(baseContext, "취소 버튼 클릭!", Toast.LENGTH_SHORT).show()
                    }
                })

                .create()
            builder.show()
        }
    }
}