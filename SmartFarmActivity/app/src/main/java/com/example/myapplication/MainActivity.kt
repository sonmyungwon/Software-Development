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


class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val modeRef = database.getReference("user/mode")
    private val ledRef = database.getReference("user/device/led")
    private val fanRef = database.getReference("user/device/fan")
    private val pumpRef = database.getReference("user/device/pump")
    private val exceptRef = database.getReference("user/exception")

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //자동제어 버튼을 눌렀을때 자동제어 액티비티(AutoControlActivity)로 넘어갑니다.
        val autoControlBtn = findViewById<Button>(R.id.autoControlBtn)

        autoControlBtn.setOnClickListener{
            val intent = Intent(this, AutoControlActivity::class.java)
            startActivity(intent)
        }

        val manualControlBtn = findViewById<Button>(R.id.manualControlBtn)
        //수동제어 버튼을 눌렀을때 나오는 다이얼로그입니다.
        //(manual_control_dialog.xml) 화면을 띄웁니다.
        manualControlBtn.setOnClickListener {

            val mDialogView = LayoutInflater.from(this).inflate(R.layout.manual_control_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("수동 제어 설정")

            val mAlertDialog = mBuilder.show()

            //만약 장치가 이미 켜져있다면 해당하는 값을 가져와 이전 스위치 상태로 바꿉니다.
            //ledRef, fanRef, pumpRef 동일합니다
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

            //각각의 스위치를 눌렀을때 on off 하고 mode 노드를 수동제어 모드로 수정합니다.
            mAlertDialog.findViewById<Switch>(R.id.ledSwitch)?.setOnCheckedChangeListener{_, onSwitch->
                if(onSwitch){
                    modeRef.setValue(1)
                    ledRef.setValue(1)
                    progressScreen(1,"user/device/real_led_on")
                }
                else {
                    ledRef.setValue(0)
                    modeRef.setValue(1)
                    progressScreen(2, "user/device/real_led_on")
                }
            }
            mAlertDialog.findViewById<Switch>(R.id.fanSwitch)?.setOnCheckedChangeListener{_, onSwitch->
                if(onSwitch){
                    fanRef.setValue(1)
                    modeRef.setValue(1)
                    progressScreen(1, "user/device/real_fan_on")
                }else{
                    fanRef.setValue(0)
                    modeRef.setValue(1)
                    progressScreen(2, "user/device/real_fan_on")
                }
            }
            mAlertDialog.findViewById<Switch>(R.id.pumpSwitch)?.setOnCheckedChangeListener { _, onSwitch->
                if(onSwitch){
                    pumpRef.setValue(1)
                    modeRef.setValue(1)
                    progressScreen(1, "user/device/real_pump_on")

                }else{
                    pumpRef.setValue(0)
                    modeRef.setValue(1)
                    progressScreen(2, "user/device/real_pump_on")
                }
            }

            //수분 과다 공급시(파이어베이스 트리 exception 노드의 값이 1일때) 펌프 정지 후 사용자에게 알림창을 띄웁니다.
            //확인을 누르면 파이어베이스 트리 exception 노드를 0으로 수정합니다.
            exceptRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Data", snapshot.value.toString())

                    if(snapshot.value.toString().toInt() == 1){
                        val builder = AlertDialog.Builder(this@MainActivity)
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
                }
            })
        }

        //관찰일지 버튼을 눌렀을때 관찰일지 화면으로 이동합니다.
        val diaryBtn = findViewById<Button>(R.id.diaryBtn)
        diaryBtn.setOnClickListener{
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
    }
    //수동제어 중 아두이노 장치가 켜지거나 꺼졌는지 확인하고 로딩창을 종료하는 메서드입니다.
    private fun progressScreen(progressMode:Int, path: String){
        val realRef = database.getReference("$path")
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching")
        progressDialog.setCancelable(false)
        progressDialog.show()
        when (progressMode) {
            1 -> {
                realRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 1){
                            progressDialog.dismiss()
                            Toast.makeText(this@MainActivity, "switch on", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            2 -> {
                realRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value.toString().toInt() == 0){
                            progressDialog.dismiss()
                            Toast.makeText(this@MainActivity, "switch off", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }

    }
}