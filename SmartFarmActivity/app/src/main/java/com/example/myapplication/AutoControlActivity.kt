package com.example.myapplication

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AutoControlActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_control)

        val tempView : TextView = findViewById<TextView>(R.id.tempText)
        val tempPlusBtn : Button = findViewById<Button>(R.id.temp_plus_btn)
        val tempMinusBtn = findViewById<Button>(R.id.temp_minus_btn)
        val state_temp_text : TextView = findViewById<TextView>(R.id.state_temp)
        val data_temp_text : TextView = findViewById<TextView>(R.id.data_temp)


        val humidView : TextView = findViewById<TextView>(R.id.humidText)
        val humidPlusBtn  = findViewById<Button>(R.id.humid_plus_btn)
        val humidMinusBtn = findViewById<Button>(R.id.humid_minus_btn)
        val state_soil_humi_text : TextView = findViewById<TextView>(R.id.state_soil_humi)
        val data_soil_humi_text : TextView = findViewById<TextView>(R.id.data_soil_humi)


        val illumView : TextView = findViewById<TextView>(R.id.illumText)
        val illumPlusBtn  = findViewById<Button>(R.id.illum_plus_btn)
        val illumMinusBtn = findViewById<Button>(R.id.illum_minus_btn)
        val state_light_text : TextView = findViewById<TextView>(R.id.state_light)
        val data_light_text : TextView = findViewById<TextView>(R.id.data_light)


        val saveBtn = findViewById<Button>(R.id.save_btn)

        val tempProgressBar = findViewById<ProgressBar>(R.id.temp_progress_bar)
        val soilProgressBar = findViewById<ProgressBar>(R.id.soil_progress_bar)
        val lightProgressBar = findViewById<ProgressBar>(R.id.light_progress_bar)


        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("user/auto/userdata/temp")
        val myRef2 = database.getReference("user/auto/userdata/soil_humi")
        val myRef3 = database.getReference("user/auto/userdata/light")

        var tempNumber: Int = tempView.text.toString().toInt()
        var humidNumber: Int = humidView.text.toString().toInt()
        var illumNumber: Int = illumView.text.toString().toInt()

        //사용자 설정 데이터 파이어베이스로부터 불러오기
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("@@@@@ tempView @@@@@@", snapshot.value.toString())
                tempView.text = snapshot.value.toString()
                tempNumber = tempView.text.toString().toInt()
                tempProgressBar.incrementProgressBy(snapshot.value.toString().toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef2.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                humidView.text = snapshot.value.toString()
                humidNumber = humidView.text.toString().toInt()
                soilProgressBar.incrementProgressBy(snapshot.value.toString().toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef3.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                illumView.text = snapshot.value.toString()
                illumNumber = illumView.text.toString().toInt()
                lightProgressBar.incrementProgressBy(snapshot.value.toString().toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        Log.d("@@@@@ tempNumber @@@@@@", tempNumber.toString())

        tempMinusBtn.setOnClickListener{
            tempNumber -= 5

            if(tempNumber in 0..100) {
                tempView.text = "$tempNumber"
                tempProgressBar.incrementProgressBy(-5)
                showState(tempNumber, state_temp_text)
                convert_data(10.0, 30.0, tempNumber, data_temp_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                tempNumber += 5

            }
        }
        tempPlusBtn.setOnClickListener {
            tempNumber += 5

            if(tempNumber in 0..100) {

                tempView.text = "$tempNumber"
                tempProgressBar.incrementProgressBy(5)
                showState(tempNumber, state_temp_text)
                convert_data(10.0, 30.0, tempNumber, data_temp_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                tempNumber -= 5

            }
        }


        humidMinusBtn.setOnClickListener{
            humidNumber -= 5

            if(humidNumber in 0..100) {

                humidView.text = "$humidNumber"
                soilProgressBar.incrementProgressBy(-5)
                showState(humidNumber, state_soil_humi_text)
                convert_data(10.0, 30.0, humidNumber, data_soil_humi_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                humidNumber += 5

            }
        }
        humidPlusBtn.setOnClickListener {
            humidNumber += 5

            if(humidNumber in 0..100) {

                humidView.text = "$humidNumber"
                soilProgressBar.incrementProgressBy(5)
                showState(humidNumber, state_soil_humi_text)
                convert_data(10.0, 30.0, humidNumber, data_soil_humi_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                humidNumber -= 5

            }
        }

        illumMinusBtn.setOnClickListener{
            illumNumber -= 5

            if(illumNumber in 0..100) {

                illumView.text = "$illumNumber"
                lightProgressBar.incrementProgressBy(-5)
                showState(illumNumber, state_light_text)
                convert_data(10.0, 30.0, illumNumber, data_light_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                illumNumber += 5
            }
        }
        illumPlusBtn.setOnClickListener {
            illumNumber += 5

            if(illumNumber in 0..100) {

                illumView.text = "$illumNumber"
                lightProgressBar.incrementProgressBy(5)
                showState(illumNumber, state_light_text)
                convert_data(10.0, 30.0, illumNumber, data_light_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                illumNumber -= 5

            }
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
                .setMessage("설정 값을 바꾸시겠습니까? \n" +
                        "                온도 : $tempNumber\n" +
                        "                토양 습도 : $humidNumber\n" +
                        "                밝기 : $illumNumber")
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

    private fun showState(num: Int, txtview: TextView){
        if(num>=80){
            txtview.setText("아주 높음")
            txtview.setTextColor(Color.RED)
        }
        else if(num>=60){
            txtview.setText("조금 높음")
            txtview.setTextColor(Color.MAGENTA)
        }
        else if(num>=40){
            txtview.setText("적당")
            txtview.setTextColor(Color.GREEN)
        }
        else if(num>=20){
            txtview.setText("조금 낮음")
            txtview.setTextColor(Color.BLUE)
        }
        else if(num>=0){
            txtview.setText("아주 낮음")
            txtview.setTextColor(Color.GRAY)
        }

    }

    private fun convert_data(min: Double, max: Double, num: Int,txtview: TextView){
        val rate : Double = (max - min)/100
        val convert_num : Double = min + rate * num
        txtview.setText("$convert_num")
    }

}