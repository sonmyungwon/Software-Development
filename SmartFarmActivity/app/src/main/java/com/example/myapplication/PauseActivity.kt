package com.example.myapplication

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.widget.Toast


class PauseActivity : AppCompatActivity() {

    class LoadingDialog(context: MainActivity2) : Dialog(context){

        init {

            setCanceledOnTouchOutside(false)

            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.activity_pause)
            //초 뒤 dismiss 하기?
            Handler(Looper.getMainLooper()).postDelayed({
                this.dismiss()
            }, 3000)
        }

    }


}