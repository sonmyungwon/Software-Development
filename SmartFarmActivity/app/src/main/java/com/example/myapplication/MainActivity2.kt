@file:Suppress("DEPRECATION")

package com.example.myapplication

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class MainActivity2 : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val modeRef = database.getReference("user/mode")
    private val ledRef = database.getReference("user/device/led")
    private val fanRef = database.getReference("user/device/fan")
    private val pumpRef = database.getReference("user/device/pump")
    private val exceptRef = database.getReference("user/exception")
    private val realLedRef = database.getReference("user/device/real_led_on")
    private val realFanRef = database.getReference("user/device/real_fan_on")
    private val realPumpRef = database.getReference("user/device/real_pump_on")


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        //자동제어 버튼을 눌렀을때 자동제어 액티비티(AutoControlActivity)로 넘어감
        val autoControlBtn = findViewById<Button>(R.id.autoControlBtn)

        autoControlBtn.setOnClickListener{
            val intent = Intent(this, AutoControlActivity::class.java)
            startActivity(intent)
        }

        //수동제어 버튼을 눌렀을때 나오는 다이얼로그
        val manualControlBtn = findViewById<Button>(R.id.manualControlBtn)

        manualControlBtn.setOnClickListener {

            val mDialogView = LayoutInflater.from(this).inflate(R.layout.manual_control_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("수동 제어 설정")

            val mAlertDialog = mBuilder.show()

            //만약 이미 켜져있다면 값을 가져와 스위치 상태 바꾸기
            ledRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("led@@@@@@@@@@", snapshot.value.toString())
                    if (snapshot.value.toString().toInt() == 1) {
                        mAlertDialog.findViewById<Switch>(R.id.ledSwitch)?.isChecked = true
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
            fanRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("fan@@@@@@@@@@", snapshot.value.toString())
                    if(snapshot.value.toString().toInt() == 1){
                        mAlertDialog.findViewById<Switch>(R.id.fanSwitch)?.isChecked = true
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
            pumpRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("pump@@@@@@@@@@", snapshot.value.toString())
                    if(snapshot.value.toString().toInt() == 1){
                        mAlertDialog.findViewById<Switch>(R.id.pumpSwitch)?.isChecked = true
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

            //각각의 스위치를 눌렀을때 on off 하기
            mAlertDialog.findViewById<Switch>(R.id.ledSwitch)?.setOnCheckedChangeListener{_, onSwitch->
                if(onSwitch){
                    modeRef.setValue(1)
                    ledRef.setValue(1)
                    progressScreen(1)
                }
                else {
                    ledRef.setValue(0)
                    modeRef.setValue(1)
                    progressScreen(2)
                }
            }

            mAlertDialog.findViewById<Switch>(R.id.fanSwitch)?.setOnCheckedChangeListener{_, onSwitch->
                if(onSwitch){
                    fanRef.setValue(1)
                    modeRef.setValue(1)
                    progressScreen(3)
                }else{
                    fanRef.setValue(0)
                    modeRef.setValue(1)
                    progressScreen(4)
                }
            }

            mAlertDialog.findViewById<Switch>(R.id.pumpSwitch)?.setOnCheckedChangeListener { _, onSwitch->
                if(onSwitch){
                    pumpRef.setValue(1)
                    modeRef.setValue(1)
                    progressScreen(5)

                }else{
                    pumpRef.setValue(0)
                    modeRef.setValue(1)
                    progressScreen(6)
                }
            }

            //수분 과다 공급시 펌프 정지
            exceptRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Data", snapshot.value.toString())

                    if(snapshot.value.toString().toInt() == 1){
                        val builder = AlertDialog.Builder(this@MainActivity2)
                        builder
                            .setTitle("알림")
                            .setMessage("exception! \n과다수분공급으로 인해 물펌프가 정지되었습니다.")
                            .setCancelable(false)

                            .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    exceptRef.setValue(0)
                                    Toast.makeText(baseContext, "확인", Toast.LENGTH_SHORT).show()
                                }
                            })
                            .create()
                        builder.show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        //관찰일지 버튼을 눌렀을때 넘어감
        val diaryBtn = findViewById<Button>(R.id.diaryBtn)
        diaryBtn.setOnClickListener{
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
    }
    //수동제어 중 아두이노 장치가 켜지거나 꺼졌는지 확인하는 로딩창을 종료하는 메서드
    private fun progressScreen(progressMode:Int): Int{
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching")
        progressDialog.setCancelable(false)
        progressDialog.show()
        when (progressMode) {
            1 -> {
                realLedRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 1){
                            progressDialog.dismiss()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            2 -> {
                realLedRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 0){
                            progressDialog.dismiss()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            3 -> {
                realFanRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 1){
                            progressDialog.dismiss()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            4 -> {
                realFanRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 0){
                            progressDialog.dismiss()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            5 -> {
                realPumpRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 1){
                            progressDialog.dismiss()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            6 -> {
                realPumpRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 0){
                            progressDialog.dismiss()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        if(progressMode == 1 || progressMode == 3 || progressMode == 5){
            Toast.makeText(this, "switch on", Toast.LENGTH_SHORT).show()
        }else if(progressMode == 2 || progressMode == 4 || progressMode == 6){
            Toast.makeText(this, "switch off", Toast.LENGTH_SHORT).show()
        }
        return 1
    }
}