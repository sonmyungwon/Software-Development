package com.example.myapplication

import android.widget.EditText

data class DataModel(
        val temperature: EditText,
        val soilHumidity: EditText,
        val illuminance: EditText
        )