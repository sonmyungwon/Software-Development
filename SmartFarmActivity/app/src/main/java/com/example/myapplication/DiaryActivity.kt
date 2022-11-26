package com.example.myapplication

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Calendar
import java.util.GregorianCalendar

class DiaryActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private val chartDataHumidity = ArrayList<ChartData>()
    private val chartDataSoilHumid = ArrayList<ChartData>()
    private val chartDataLight = ArrayList<ChartData>()
    private val chartDataTemp = ArrayList<ChartData>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        val dateBtn = findViewById<ImageView>(R.id.dateBtn)
        val textDay = findViewById<TextView>(R.id.dayText)

        val today = GregorianCalendar()
        val year : Int = today.get(Calendar.YEAR)
        val month : Int = today.get(Calendar.MONTH)
        val date : Int = today.get(Calendar.DATE)
        //달력 아이콘을 클릭시 날짜를 설정하고 텍스트로 나타냅니다.
        dateBtn?.setOnClickListener{

            val dlg = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    Log.d("MAIN", "${year}. ${month + 1}. $dayOfMonth")
                    textDay.text = "${year}. ${month + 1}. $dayOfMonth"
                },year,month, date)

            dlg.show()
        }


        val getImage = findViewById<ImageView>(R.id.getImage)
        val imageView = findViewById<ImageView>(R.id.storageImage)
        //적용 버튼 클릭 시 파이어스토어 스토리지에서 해당 날짜의 사진을 가져옵니다.
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
        val humidityRef = database.getReference("user/sensor/221123/humi")
        val soilHumidityRef = database.getReference("user/sensor/221123/soil_humi")
        val tempRef = database.getReference("user/sensor/221123/temp")
        val lightRef = database.getReference("user/sensor/221123/light")

        //각 센서값의 그래프 그리기 humidityRef, soilHumidityRef, tempRef, lightRef
        humidityRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                makeGraph(snapshot, chartDataHumidity, "humidity")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        soilHumidityRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                makeGraph(snapshot,chartDataSoilHumid,"soil_humidity")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        tempRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                makeGraph(snapshot,chartDataTemp,"temperature")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        lightRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                makeGraph(snapshot,chartDataLight,"light")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    //그래프의 값을 chartData배열에 저장합니다.
    private fun addChartItem(lableItem: String, dataItem: Double, chartData: ArrayList<ChartData>) {
        val item = ChartData()
        item.lable = lableItem
        item.lineData = dataItem
        chartData.add(item)
    }
    //저장되어있는 배열을 이용하여 그래프를 작성합니다.
    private fun lineChart(chartData: ArrayList<ChartData>, name : String) {
        when (name) {
            "humidity" -> lineChart = findViewById(R.id.linechart_humidity)
            "soil_humidity" -> lineChart = findViewById(R.id.linechart_soil_humi)
            "light" -> lineChart = findViewById(R.id.linechart_light)
            "temperature" -> lineChart = findViewById(R.id.linechart_temp)
        }

        val entries = mutableListOf<Entry>()  //차트 데이터 셋에 담겨질 데이터

        for (item in chartData) {
            entries.add(Entry(item.lable.replace(("[^\\d.]").toRegex(), "").toFloat(), item.lineData.toFloat()))
        }

        //LineDataSet 선언
        val lineDataSet = LineDataSet(entries, name)

        when (name) {//LineChart에서 Line Color 설정
            "humidity" -> lineDataSet.color = Color.BLUE
            "soil_humidity" -> lineDataSet.color = Color.RED
            "light" -> lineDataSet.color = Color.GREEN
            "temperature" -> lineDataSet.color = Color.YELLOW
        }

        lineDataSet.circleHoleColor = Color.TRANSPARENT  // LineChart에서 Line Circle Color 설정
        lineDataSet.circleHoleColor = Color.TRANSPARENT // LineChart에서 Line Hole Circle Color 설정

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet) // 데이터 셋 추가

        val data = LineData(dataSets)

        // 데이터 설정
        lineChart.data = data
        lineChart.description = null //차트에서 Description 설정 삭제
        //XAxis.XAxisPosition.BOTTOM // 라벨 위치 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.invalidate()
    }

    fun makeGraph(snapshot: DataSnapshot, arrayList: ArrayList<ChartData>,name:String){
        var array = snapshot.value.toString().split("{" , ", " , "}").sorted()
        for (i:Int in 2 until array.size) {
            addChartItem("${((i).toDouble()-2)/2}", array[i].split("=")[1].toDouble()+0 , arrayList)
        }
        lineChart(arrayList,name)
    }
}
//그래프의 값이 저장되는 배열에 저장될 데이터 클래스
data class ChartData(
    var lable: String = "",
    var valData: Double = 0.0,
    var lineData: Double = 0.0
)