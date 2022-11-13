package com.example.myapplication

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

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



        var tempNumber: Int = tempView.text.toString().toInt()

        tempMinusBtn.setOnClickListener{
            tempNumber -= 5

            if(tempNumber in 0..100) {
                tempView.setText("$tempNumber")
                showGauge(tempNumber, 1)
                show_state(tempNumber, state_temp_text)
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

                tempView.setText("$tempNumber")
                showGauge(tempNumber, 1)
                show_state(tempNumber, state_temp_text)
                convert_data(10.0, 30.0, tempNumber, data_temp_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                tempNumber -= 5

            }
        }

        var humidNumber: Int = humidView.text.toString().toInt()

        humidMinusBtn.setOnClickListener{
            humidNumber -= 5

            if(humidNumber in 0..100) {

                humidView.setText("$humidNumber")
                showGauge(humidNumber, 2)
                show_state(humidNumber, state_soil_humi_text)
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

                humidView.setText("$humidNumber")
                showGauge(humidNumber, 2)
                show_state(humidNumber, state_soil_humi_text)
                convert_data(10.0, 30.0, humidNumber, data_soil_humi_text)
            }
            else{
                Toast.makeText(this@AutoControlActivity, "범위 초과", Toast.LENGTH_SHORT).show()
                humidNumber -= 5

            }
        }

        var illumNumber: Int = illumView.text.toString().toInt()

        illumMinusBtn.setOnClickListener{
            illumNumber -= 5

            if(illumNumber in 0..100) {

                illumView.setText("$illumNumber")
                showGauge(illumNumber, 3)
                show_state(illumNumber, state_light_text)
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

                illumView.setText("$illumNumber")
                showGauge(illumNumber, 3)
                show_state(illumNumber, state_light_text)
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

    private fun show_state(num: Int, txtview: TextView){
        if(num>=80)
            txtview.setText("아주 높음")


        else if(num>=60)
            txtview.setText("조금 높음")

        else if(num>=40)
            txtview.setText("적당")

        else if(num>=20)
            txtview.setText("조금 낮음")

        else if(num>=0)
            txtview.setText("아주 낮음")



    }

    private fun convert_data(min: Double, max: Double, num: Int,txtview: TextView){
        var rate : Double = (max - min)/100
        var convert_num : Double = min + rate * num
        txtview.setText("$convert_num")
    }

    private fun showGauge(num: Int, typeNum: Int): Boolean {
        val per1 = findViewById<TextView>(R.id.per1)
        val per2 = findViewById<TextView>(R.id.per2)
        val per3 = findViewById<TextView>(R.id.per3)
        val per4 = findViewById<TextView>(R.id.per4)
        val per5 = findViewById<TextView>(R.id.per5)
        val per6 = findViewById<TextView>(R.id.per6)
        val per7 = findViewById<TextView>(R.id.per7)
        val per8 = findViewById<TextView>(R.id.per8)
        val per9 = findViewById<TextView>(R.id.per9)
        val per10 = findViewById<TextView>(R.id.per10)
        val per11 = findViewById<TextView>(R.id.per11)
        val per12 = findViewById<TextView>(R.id.per12)
        val per13 = findViewById<TextView>(R.id.per13)
        val per14 = findViewById<TextView>(R.id.per14)
        val per15 = findViewById<TextView>(R.id.per15)
        val per16 = findViewById<TextView>(R.id.per16)
        val per17 = findViewById<TextView>(R.id.per17)
        val per18 = findViewById<TextView>(R.id.per18)
        val per19 = findViewById<TextView>(R.id.per19)
        val per20 = findViewById<TextView>(R.id.per20)
        val per21 = findViewById<TextView>(R.id.per21)
        val per22 = findViewById<TextView>(R.id.per22)
        val per23 = findViewById<TextView>(R.id.per23)
        val per24 = findViewById<TextView>(R.id.per24)
        val per25 = findViewById<TextView>(R.id.per25)
        val per26 = findViewById<TextView>(R.id.per26)
        val per27 = findViewById<TextView>(R.id.per27)
        val per28 = findViewById<TextView>(R.id.per28)
        val per29 = findViewById<TextView>(R.id.per29)
        val per30 = findViewById<TextView>(R.id.per30)

        if (typeNum == 1) {
            if (num <= 5) {
                per1.visibility = View.INVISIBLE
                per2.visibility = View.INVISIBLE
                per3.visibility = View.INVISIBLE
                per4.visibility = View.INVISIBLE
                per5.visibility = View.INVISIBLE
                per6.visibility = View.INVISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
            }
            if (num in 6..15) {
                per1.visibility = View.VISIBLE
                per2.visibility = View.INVISIBLE
                per3.visibility = View.INVISIBLE
                per4.visibility = View.INVISIBLE
                per5.visibility = View.INVISIBLE
                per6.visibility = View.INVISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 16..25) {
                per2.visibility = View.VISIBLE
                per3.visibility = View.INVISIBLE
                per4.visibility = View.INVISIBLE
                per5.visibility = View.INVISIBLE
                per6.visibility = View.INVISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 26..35) {
                per3.visibility = View.VISIBLE
                per4.visibility = View.INVISIBLE
                per5.visibility = View.INVISIBLE
                per6.visibility = View.INVISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 36..45) {
                per4.visibility = View.VISIBLE
                per5.visibility = View.INVISIBLE
                per6.visibility = View.INVISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 46..55) {
                per5.visibility = View.VISIBLE
                per6.visibility = View.INVISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 56..65) {
                per6.visibility = View.VISIBLE
                per7.visibility = View.INVISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 66..75) {
                per7.visibility = View.VISIBLE
                per8.visibility = View.INVISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 76..85) {
                per8.visibility = View.VISIBLE
                per9.visibility = View.INVISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 86..95) {
                per9.visibility = View.VISIBLE
                per10.visibility = View.INVISIBLE
                return true
            }
            if (num in 96..100) {
                per10.visibility = View.VISIBLE
                return true
            }
        }
        if (typeNum == 2) {
            if (num <= 5) {
                per11.visibility = View.INVISIBLE
                per12.visibility = View.INVISIBLE
                per13.visibility = View.INVISIBLE
                per14.visibility = View.INVISIBLE
                per15.visibility = View.INVISIBLE
                per16.visibility = View.INVISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
            }
            if (num in 6..15) {
                per11.visibility = View.VISIBLE
                per12.visibility = View.INVISIBLE
                per13.visibility = View.INVISIBLE
                per14.visibility = View.INVISIBLE
                per15.visibility = View.INVISIBLE
                per16.visibility = View.INVISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 16..25) {
                per12.visibility = View.VISIBLE
                per13.visibility = View.INVISIBLE
                per14.visibility = View.INVISIBLE
                per15.visibility = View.INVISIBLE
                per16.visibility = View.INVISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 26..35) {
                per13.visibility = View.VISIBLE
                per14.visibility = View.INVISIBLE
                per15.visibility = View.INVISIBLE
                per16.visibility = View.INVISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 36..45) {
                per14.visibility = View.VISIBLE
                per15.visibility = View.INVISIBLE
                per16.visibility = View.INVISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 46..55) {
                per15.visibility = View.VISIBLE
                per16.visibility = View.INVISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 56..65) {
                per16.visibility = View.VISIBLE
                per17.visibility = View.INVISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 66..75) {
                per17.visibility = View.VISIBLE
                per18.visibility = View.INVISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 76..85) {
                per18.visibility = View.VISIBLE
                per19.visibility = View.INVISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 86..95) {
                per19.visibility = View.VISIBLE
                per20.visibility = View.INVISIBLE
                return true
            }
            if (num in 96..100) {
                per20.visibility = View.VISIBLE
                return true
            }
        }
        if(typeNum == 3){
            if (num <= 5) {
                per21.visibility = View.INVISIBLE
                per22.visibility = View.INVISIBLE
                per23.visibility = View.INVISIBLE
                per24.visibility = View.INVISIBLE
                per25.visibility = View.INVISIBLE
                per26.visibility = View.INVISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
            }
            if (num in 6..15) {
                per21.visibility = View.VISIBLE
                per22.visibility = View.INVISIBLE
                per23.visibility = View.INVISIBLE
                per24.visibility = View.INVISIBLE
                per25.visibility = View.INVISIBLE
                per26.visibility = View.INVISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 16..25) {
                per22.visibility = View.VISIBLE
                per23.visibility = View.INVISIBLE
                per24.visibility = View.INVISIBLE
                per25.visibility = View.INVISIBLE
                per26.visibility = View.INVISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 26..35) {
                per23.visibility = View.VISIBLE
                per24.visibility = View.INVISIBLE
                per25.visibility = View.INVISIBLE
                per26.visibility = View.INVISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 36..45) {
                per24.visibility = View.VISIBLE
                per25.visibility = View.INVISIBLE
                per26.visibility = View.INVISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 46..55) {
                per25.visibility = View.VISIBLE
                per26.visibility = View.INVISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 56..65) {
                per26.visibility = View.VISIBLE
                per27.visibility = View.INVISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 66..75) {
                per27.visibility = View.VISIBLE
                per28.visibility = View.INVISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 76..85) {
                per28.visibility = View.VISIBLE
                per29.visibility = View.INVISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 86..95) {
                per29.visibility = View.VISIBLE
                per30.visibility = View.INVISIBLE
                return true
            }
            if (num in 96..100) {
                per30.visibility = View.VISIBLE
                return true
            }
        }
        return false
    }
}