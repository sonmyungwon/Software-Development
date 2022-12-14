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
import kotlin.math.roundToInt

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
        //??????????????? ???
        val tempProgressBar: ProgressBar = findViewById(R.id.temp_progress_bar)
        val soilProgressBar: ProgressBar = findViewById(R.id.soil_progress_bar)
        val lightProgressBar: ProgressBar = findViewById(R.id.light_progress_bar)

        //????????? ????????? ????????? ?????? ????????? ?????? ???????????????.

        var tempNumber: Int = tempView.text.toString().toInt()
        var humidNumber: Int = humidView.text.toString().toInt()
        var lightNumber: Int = lightView.text.toString().toInt()

        var dataTempNumber: Double = dataTempText.text.toString().toDouble()
        var dataHumidNumber: Double = dataSoilHumidText.text.toString().toDouble()
        var dataLightNumber: Double = dataLightText.text.toString().toDouble()

        //?????? ?????? ???????????? ???????????? ??????????????? ?????? ?????? ????????? ?????? ???????????? ??????????????????????????? ???????????????
        //tempRef, soilHumidityRef, lightRef??? 3?????? ??????????????? ?????? ????????? ?????? ????????? ?????? ?????????. cnum????????? ????????????????????? ?????? ??????????????? ??????
        tempRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var cnum: Int = (snapshot.value.toString().toInt() - 10)*5
                tempView.text = cnum.toString()
                tempNumber = tempView.text.toString().toInt()
                tempProgressBar.progress = cnum.toString().toInt()
                showState(tempNumber, stateTempText)
                dataTempNumber = convertData(10.0, 30.0, tempNumber, dataTempText)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        soilHumidityRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var cnum: Int = ((snapshot.value.toString().toDouble().roundToInt() - 1020)*100/-520)
                humidView.text = cnum.toString()
                humidNumber = humidView.text.toString().toInt()
                soilProgressBar.progress = cnum.toString().toInt()
                showState(humidNumber, stateSoilHumidText)
                dataHumidNumber = convertData(1020.0, 500.0, humidNumber, dataSoilHumidText)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        lightRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var cnum: Int = (snapshot.value.toString().toInt() - 300)*100/600
                lightView.text = cnum.toString()
                lightNumber = lightView.text.toString().toInt()
                lightProgressBar.progress = cnum.toString().toInt()
                showState(lightNumber, stateLightText)
                dataLightNumber = convertData(300.0, 900.0, lightNumber, dataLightText)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        //????????? ???????????? ?????? ????????? ??????????????? ??? ??????, ?????? ?????????(ex. ??????, ?????? ??????, ???,,) ??????,
        //????????? ????????? ????????? ?????? ??? ?????? ????????? ????????? ???????????? ???????????????.
        tempMinusBtn.setOnClickListener{
            tempNumber -= 5
            if (tempNumber in 0..100) {
                tempView.text = "$tempNumber"
                tempProgressBar.incrementProgressBy(-5)
                showState(tempNumber, stateTempText)
                dataTempNumber = convertData(10.0, 30.0, tempNumber, dataTempText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "?????? ??????", Toast.LENGTH_SHORT).show()
                tempNumber += 5
            }
        }
        tempPlusBtn.setOnClickListener {
            tempNumber += 5
            if (tempNumber in 0..100) {
                tempView.text = "$tempNumber"
                tempProgressBar.incrementProgressBy(5)
                showState(tempNumber, stateTempText)
                dataTempNumber = convertData(10.0, 30.0, tempNumber, dataTempText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "?????? ??????", Toast.LENGTH_SHORT).show()
                tempNumber -= 5
            }
        }
        humidMinusBtn.setOnClickListener{
            humidNumber -= 5
            if (humidNumber in 0..100) {
                humidView.text = "$humidNumber"
                soilProgressBar.incrementProgressBy(-5)
                showState(humidNumber, stateSoilHumidText)
                dataHumidNumber = convertData(1020.0, 500.0, humidNumber, dataSoilHumidText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "?????? ??????", Toast.LENGTH_SHORT).show()
                humidNumber += 5
            }
        }
        humidPlusBtn.setOnClickListener {
            humidNumber += 5
            if (humidNumber in 0..100) {
                humidView.text = "$humidNumber"
                soilProgressBar.incrementProgressBy(5)
                showState(humidNumber, stateSoilHumidText)
                dataHumidNumber = convertData(1020.0, 500.0, humidNumber, dataSoilHumidText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "?????? ??????", Toast.LENGTH_SHORT).show()
                humidNumber -= 5
            }
        }
        lightMinusBtn.setOnClickListener{
            lightNumber -= 5
            if (lightNumber in 0..100) {
                lightView.text = "$lightNumber"
                lightProgressBar.incrementProgressBy(-5)
                showState(lightNumber, stateLightText)
                dataLightNumber = convertData(300.0, 900.0, lightNumber, dataLightText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "?????? ??????", Toast.LENGTH_SHORT).show()
                lightNumber += 5
            }
        }
        lightPlusBtn.setOnClickListener {
            lightNumber += 5
            if (lightNumber in 0..100) {
                lightView.text = "$lightNumber"
                lightProgressBar.incrementProgressBy(5)
                showState(lightNumber, stateLightText)
                dataLightNumber = convertData(300.0, 900.0, lightNumber, dataLightText)
            }
            else {
                Toast.makeText(this@AutoControlActivity, "?????? ??????", Toast.LENGTH_SHORT).show()
                lightNumber -= 5
            }
        }
        //???????????? ????????? ????????? ???????????? ?????????,
        saveBtn.setOnClickListener{

            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("??????")
                .setMessage("?????? ?????? ?????????????????????? \n" +
                        " ??????      : $dataTempNumber\n" +
                        " ?????? ??????  : $dataHumidNumber\n" +
                        " ??????      : $dataLightNumber")
                .setCancelable(false)
                .setPositiveButton("??????") { _, _ ->
                    sendUserSetting(dataTempNumber, dataHumidNumber, dataLightNumber)
                    Toast.makeText(baseContext, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("??????"
                ) { _, _ -> Toast.makeText(baseContext, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show() }
                .create()
            builder.show()
        }
    }
    //?????????????????? ?????????????????? ?????? ?????? userdata??? ?????? ???????????????.
    private fun sendUserSetting(tempNumber: Double, humidNumber: Double, lightNumber: Double){
        tempRef.setValue(tempNumber)
        soilHumidityRef.setValue(humidNumber)
        lightRef.setValue(lightNumber)
        modeRef.setValue(2)
    }
    //??????????????? ??? ??? ????????? ???????????? ???????????? ????????? ?????????.
    private fun showState(num: Int, textview: TextView){
        if (num>=80) {
            textview.text = "?????? ??????"
            textview.setTextColor(Color.parseColor("#FD0000"))
        }
        else if (num>=60) {
            textview.text = "?????? ??????"
            textview.setTextColor(Color.parseColor("#C16161"))
        }
        else if (num>=40) {
            textview.text = "??????"
            textview.setTextColor(Color.parseColor("#04B470"))
        }
        else if (num>=20) {
            textview.text = "?????? ??????"
            textview.setTextColor(Color.parseColor("#00462B"))
        }
        else if (num>=0) {
            textview.text = "?????? ??????"
            textview.setTextColor(Color.GRAY)
        }
    }
    //????????? ?????? ?????? ??? ????????? ???????????? ??????????????????.
    private fun convertData(min: Double, max: Double, num: Int, textview: TextView): Double {
        val rate : Double = (max - min)/100
        val convertNum : Double = ((min + rate * num)*100).roundToInt()/100.0
        textview.text = "$convertNum"
        return convertNum
    }
}