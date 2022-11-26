package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AutoControlActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val tempRef = database.getReference("user/userdata/temp")
    val soilHumidityRef = database.getReference("user/userdata/soil_humi")
    val lightRef = database.getReference("user/userdata/light")
    val modeRef = database.getReference("user/mode")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_control)

        val tempView : TextView = findViewById(R.id.tempText)
        val tempPlusBtn : Button = findViewById(R.id.temp_plus_btn)
        val tempMinusBtn: Button = findViewById(R.id.temp_minus_btn)
        val stateTempText : TextView = findViewById(R.id.state_temp)
        val dataTempText : TextView = findViewById(R.id.data_temp)

        val humidView : TextView = findViewById(R.id.humidText)
        val humidPlusBtn: Button = findViewById(R.id.humid_plus_btn)
        val humidMinusBtn: Button = findViewById(R.id.humid_minus_btn)
        val stateSoilHumidText : TextView = findViewById(R.id.state_soil_humi)
        val dataSoilHumidText : TextView = findViewById(R.id.data_soil_humi)

        val lightView : TextView = findViewById(R.id.illumText)
        val lightPlusBtn  = findViewById<Button>(R.id.illum_plus_btn)
        val lightMinusBtn = findViewById<Button>(R.id.illum_minus_btn)
        val stateLightText : TextView = findViewById(R.id.state_light)
        val dataLightText : TextView = findViewById(R.id.data_light)

        val saveBtn: Button = findViewById(R.id.save_btn)
        //프로그래스 바
        val tempProgressBar: ProgressBar = findViewById(R.id.temp_progress_bar)
        val soilProgressBar: ProgressBar = findViewById(R.id.soil_progress_bar)
        val lightProgressBar: ProgressBar = findViewById(R.id.light_progress_bar)

        //파이어 베이스 데이터 트리 경로의 값을 저장합니다.


        var tempNumber: Int = tempView.text.toString().toInt()
        var humidNumber: Int = humidView.text.toString().toInt()
        var lightNumber: Int = lightView.text.toString().toInt()

        //자동 제어 화면으로 들어오면 프로그래스 바는 이전 사용자 설정 데이터를 파이어베이스로부터 불러옵니다
        //myRef, myRef2, myRef3는 3개의 프로그래스 바의 이전 상태로 설정 합니다.
        tempRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                tempView.text = snapshot.value.toString()
                tempNumber = tempView.text.toString().toInt()
                tempProgressBar.incrementProgressBy(snapshot.value.toString().toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        soilHumidityRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                humidView.text = snapshot.value.toString()
                humidNumber = humidView.text.toString().toInt()
                soilProgressBar.incrementProgressBy(snapshot.value.toString().toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        lightRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                lightView.text = snapshot.value.toString()
                lightNumber = lightView.text.toString().toInt()
                lightProgressBar.incrementProgressBy(snapshot.value.toString().toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        //플러스 마이너스 버튼 클릭시 프로그래스 바 작동, 상태 텍스트(ex. 적당, 매우 많음, 등,,) 변환,
        //그리고 사용자 데이터 변환 후 실제 데이터 단위를 텍스르로 나타냅니다.
        tempMinusBtn.setOnClickListener{
            tempNumber -= 5
            if (tempNumber in 0..100) {
                tempView.text = "$tempNumber"
                tempProgressBar.incrementProgressBy(-5)
                showState(tempNumber, stateTempText)
                convertData(10.0, 30.0, tempNumber, dataTempText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                tempNumber += 5
            }
        }
        tempPlusBtn.setOnClickListener {
            tempNumber += 5
            if (tempNumber in 0..100) {
                tempView.text = "$tempNumber"
                tempProgressBar.incrementProgressBy(5)
                showState(tempNumber, stateTempText)
                convertData(10.0, 30.0, tempNumber, dataTempText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                tempNumber -= 5
            }
        }
        humidMinusBtn.setOnClickListener{
            humidNumber -= 5
            if (humidNumber in 0..100) {
                humidView.text = "$humidNumber"
                soilProgressBar.incrementProgressBy(-5)
                showState(humidNumber, stateSoilHumidText)
                convertData(10.0, 30.0, humidNumber, dataSoilHumidText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                humidNumber += 5
            }
        }
        humidPlusBtn.setOnClickListener {
            humidNumber += 5
            if (humidNumber in 0..100) {
                humidView.text = "$humidNumber"
                soilProgressBar.incrementProgressBy(5)
                showState(humidNumber, stateSoilHumidText)
                convertData(10.0, 30.0, humidNumber, dataSoilHumidText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                humidNumber -= 5
            }
        }
        lightMinusBtn.setOnClickListener{
            lightNumber -= 5
            if (lightNumber in 0..100) {
                lightView.text = "$lightNumber"
                lightProgressBar.incrementProgressBy(-5)
                showState(lightNumber, stateLightText)
                convertData(10.0, 30.0, lightNumber, dataLightText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                lightNumber += 5
            }
        }
        lightPlusBtn.setOnClickListener {
            lightNumber += 5
            if (lightNumber in 0..100) {
                lightView.text = "$lightNumber"
                lightProgressBar.incrementProgressBy(5)
                showState(lightNumber, stateLightText)
                convertData(10.0, 30.0, lightNumber, dataLightText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                lightNumber -= 5
            }
        }
        //저장버튼 클릭시 사용자 알림창을 띄우고,

        saveBtn.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("알림")
                .setMessage("설정 값을 바꾸시겠습니까? \n" +
                        "                온도 : $tempNumber\n" +
                        "                토양 습도 : $humidNumber\n" +
                        "                밝기 : $lightNumber")
                .setCancelable(false)
                .setPositiveButton("확인") { _, _ ->
                    sendUserSetting(tempNumber, humidNumber, lightNumber)
                    Toast.makeText(baseContext, "확인 버튼 클릭!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소"
                ) { _, _ -> Toast.makeText(baseContext, "취소 버튼 클릭!", Toast.LENGTH_SHORT).show() }
                .create()
            builder.show()
        }
    }
    //파이어베이스 데이터베이스 트리 노드 userdata에 값을 저장합니다.
    private fun sendUserSetting(tempNumber:Int, humidNumber: Int, lightNumber:Int){
        tempRef.setValue(tempNumber)
        soilHumidityRef.setValue(humidNumber)
        lightRef.setValue(lightNumber)
        modeRef.setValue(2)
    }
    //프로그래스 바 옆 상태를 텍스트로 나타내는 메서드 입니다.
    private fun showState(num: Int, textview: TextView){
        if (num>=80) {
            textview.text = "아주 높음"
            textview.setTextColor(Color.RED)
        }
        else if (num>=60) {
            textview.text = "조금 높음"
            textview.setTextColor(Color.MAGENTA)
        }
        else if (num>=40) {
            textview.text = "적당"
            textview.setTextColor(Color.GREEN)
        }
        else if (num>=20) {
            textview.text = "조금 낮음"
            textview.setTextColor(Color.BLUE)
        }
        else if (num>=0) {
            textview.text = "아주 낮음"
            textview.setTextColor(Color.GRAY)
        }
    }
    //사용자 값을 센서 값 단위로 변환하는 메서드입니다.
    private fun convertData(min: Double, max: Double, num: Int, textview: TextView){
        val rate : Double = (max - min)/100
        val convertNum : Double = min + rate * num
        textview.text = "$convertNum"
    }
}