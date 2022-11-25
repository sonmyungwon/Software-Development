package com.example.myapplication

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Calendar
import java.util.GregorianCalendar

class DiaryActivity : AppCompatActivity() {

    lateinit var lineChart: LineChart
    private val chartDataHumidity = ArrayList<ChartData>()
    private val chartDataSoilHumid = ArrayList<ChartData>()
    private val chartDataLight = ArrayList<ChartData>()
    private val chartDataTemp = ArrayList<ChartData>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_diary)

        val dateBtn = findViewById<ImageView>(R.id.dateBtn)
        val textDay = findViewById<TextView>(R.id.dayText)

        val today = GregorianCalendar()
        val year : Int = today.get(Calendar.YEAR)
        val month : Int = today.get(Calendar.MONTH)
        val date : Int = today.get(Calendar.DATE)
        //달력 아이콘을 클릭시 날짜 설정
        dateBtn?.setOnClickListener{

            val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {

                @SuppressLint("SetTextI18n")
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
                {
                    Log.d("MAIN", "${year}. ${month + 1}. $dayOfMonth")
                    textDay.text = "${year}. ${month + 1}. $dayOfMonth"
                }

            },year,month, date)

            dlg.show()
        }

        val getImage = findViewById<ImageView>(R.id.getImage)
        val imageView = findViewById<ImageView>(R.id.storageImage)
        //적용 버튼 클릭 시 파이어스토어 스토리지에서 사진 가져오기
        getImage.setOnClickListener{
            val imageName = textDay.text.toString()
            val storageRef = FirebaseStorage.getInstance().reference.child("images/$imageName.png")
            val localFile = File.createTempFile("tempImage", "png")

            storageRef.getFile(localFile).addOnSuccessListener {

                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                imageView.setImageBitmap(bitmap)

            }.addOnFailureListener{

                Toast.makeText(this, "Failed ", Toast.LENGTH_SHORT).show()
            }
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("user/auto/sensor/20221112/humi")
        val myRef1 = database.getReference("user/auto/sensor/20221112/soil_humi")
        val myRef2 = database.getReference("user/auto/sensor/20221112/temp")
        val myRef3 = database.getReference("user/auto/sensor/221116/light")
        //각 센서값의 하루 그래프 그리기 myRef, myRef1, myRef2, myRef3
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val split = snapshot.value.toString().split("=", ", ", "}")
                for (i:Int in 0 until split.size/2) {
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartDataHumidity)
                }
                LineChart(chartDataHumidity,"humidity")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val split = snapshot.value.toString().split("=", ", ", "}")
                for (i:Int in 0 until split.size/2-1) {
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartDataSoilHumid)
                }
                LineChart(chartDataSoilHumid,"soil_humi")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val split = snapshot.value.toString().split("=", ", ", "}")
                for (i:Int in 0 until split.size/2-1) {
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartDataTemp)
                }
                LineChart(chartDataTemp,"temp")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef3.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val split = snapshot.value.toString().split("=", ", ", "}")
                for (i:Int in 0 until split.size/2-1) {
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartDataLight)
                }
                LineChart(chartDataLight,"light")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun addChartItem(lableItem: String, dataItem: Double, chartData: ArrayList<ChartData>) {
        val item = ChartData()
        item.lableData = lableItem
        item.lineData = dataItem
        chartData.add(item)
    }

    private fun LineChart(chartData: ArrayList<ChartData>, name : String) {
        if (name == "humidity")
            lineChart = findViewById(R.id.linechart_humidity)
        else if (name == "soil_humi")
            lineChart = findViewById(R.id.linechart_soil_humi)
        else if (name == "light")
            lineChart = findViewById(R.id.linechart_light)
        else if (name == "temp")
            lineChart = findViewById(R.id.linechart_temp)
        //  lineChart = findViewById(R.id.linechart_temp)

        val entries = mutableListOf<Entry>()  //차트 데이터 셋에 담겨질 데이터

        for (item in chartData) {
            entries.add(Entry(item.lableData.replace(("[^\\d.]").toRegex(), "").toFloat(), item.lineData.toFloat()))
        }

        //LineDataSet 선언
        val lineDataSet: LineDataSet
        lineDataSet = LineDataSet(entries, name)
        if (name == "humidity")
            lineDataSet.color = Color.BLUE  //LineChart에서 Line Color 설정
        else if (name == "soil_humi")
            lineDataSet.color = Color.RED
        else if (name == "light")
            lineDataSet.color = Color.GREEN
        else if (name == "temp")
            lineDataSet.color = Color.YELLOW

        lineDataSet.setCircleColor(Color.TRANSPARENT)  // LineChart에서 Line Circle Color 설정
        lineDataSet.setCircleHoleColor(Color.TRANSPARENT) // LineChart에서 Line Hole Circle Color 설정

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet) // add the data sets

        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        lineChart.setData(data)
        lineChart.setDescription(null) //차트에서 Description 설정 삭제
        //XAxis.XAxisPosition.BOTTOM // 라벨 위치 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.invalidate()
    }
}
data class ChartData(
    var lableData: String = "",
    var valData: Double = 0.0,
    var lineData: Double = 0.0
)