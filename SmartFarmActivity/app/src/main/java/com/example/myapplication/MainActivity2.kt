package com.example.myapplication

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

            //val ledText = findViewById<TextView>(R.id.led_text)
            mAlertDialog.findViewById<Switch>(R.id.ledSwitch)?.setOnCheckedChangeListener{_, onSwitch->

                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("user/manual/device/led")
                val myRef1 = database.getReference("user/mode")
                //val myRef2 = database.getReference("user/manual/device/real_led")

                if(onSwitch){
                    myRef1.setValue(1)
                    myRef.setValue(1)
                    Toast.makeText(this, "switch on", Toast.LENGTH_SHORT).show()
                    PauseActivity.LoadingDialog(this@MainActivity2).show()

                    /*CoroutineScope(Main).launch {
                        PauseActivity.LoadingDialog(this@MainActivity2).show()
                        delay(2000)
                        PauseActivity.LoadingDialog(this@MainActivity2).dismiss()
                    }*/
                }
                else {
                    myRef1.setValue(1)
                    myRef.setValue(0)
                    Toast.makeText(this, "switch off", Toast.LENGTH_SHORT).show()
                    PauseActivity.LoadingDialog(this@MainActivity2).show()

                }

            }


            mAlertDialog.findViewById<Switch>(R.id.fanSwitch)?.setOnCheckedChangeListener{_, onSwitch->

                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("user/manual/device/fan")
                val myRef1 = database.getReference("user/mode")

                if(onSwitch){
                    Toast.makeText(this, "switch on", Toast.LENGTH_SHORT).show()
                    myRef1.setValue(1)
                    myRef.setValue(1)
                    PauseActivity.LoadingDialog(this@MainActivity2).show()
                }else{
                    Toast.makeText(this, "switch off", Toast.LENGTH_SHORT).show()
                    myRef1.setValue(1)
                    myRef.setValue(0)
                    PauseActivity.LoadingDialog(this@MainActivity2).show()
                }
            }

            //val pumpText = findViewById<TextView>(R.id.pump_text)
            mAlertDialog.findViewById<Switch>(R.id.pumpSwitch)?.setOnCheckedChangeListener { _, onSwitch->
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("user/manual/device/pump")
                val myRef1 = database.getReference("user/mode")

                if(onSwitch){
                    Toast.makeText(this, "switch on", Toast.LENGTH_SHORT).show()
                    myRef1.setValue(1)
                    myRef.setValue(1)
                    PauseActivity.LoadingDialog(this@MainActivity2).show()
                }else{
                    Toast.makeText(this, "switch off", Toast.LENGTH_SHORT).show()
                    myRef1.setValue(1)
                    myRef.setValue(0)
                    PauseActivity.LoadingDialog(this@MainActivity2).show()
                }
            }

            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("user/manual/exception")


                //로그에 띄우기
            myRef.addValueEventListener(object : ValueEventListener{
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
                                    myRef.setValue(0)
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
}