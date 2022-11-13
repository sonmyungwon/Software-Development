package com.example.myapplication

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
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.joinAll
import java.io.File
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.concurrent.timer

class DiaryActivity : AppCompatActivity() {

    //private val TAG = this.javaClass.simpleName
    lateinit var lineChart: LineChart
    private val chartData_humidity = ArrayList<ChartData>()
    private val chartData_soil_humi = ArrayList<ChartData>()
    private val chartData_light = ArrayList<ChartData>()
    private val chartData_temp = ArrayList<ChartData>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_diary)

        val dateBtn = findViewById<ImageView>(R.id.dateBtn)
        val textDay = findViewById<TextView>(R.id.dayText)

        val today = GregorianCalendar()
        val year : Int = today.get(Calendar.YEAR)
        val month : Int = today.get(Calendar.MONTH)
        val date : Int = today.get(Calendar.DATE)

        dateBtn?.setOnClickListener{

            val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {

                override fun onDateSet(view: DatePicker?,year: Int, month: Int, dayOfMonth: Int)
                {
                    Log.d("MAIN", "${year}. ${month + 1}. $dayOfMonth")
                    textDay.text = "${year}. ${month + 1}. $dayOfMonth"
                }

            },year,month, date)

            dlg.show()
        }

        val getImage = findViewById<ImageView>(R.id.getImage)
        val imageView = findViewById<ImageView>(R.id.storageImage)

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
        val myRef3 = database.getReference("user/auto/sensor/20221112/light")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val split = snapshot.value.toString().split("=", ", ", "}")
                for(i:Int in 0..10){
                    //Log.d("@@@@datata@@@@@", split[i*2 + 1])
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartData_humidity)
                }

                LineChart(chartData_humidity,"humidity")

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val split = snapshot.value.toString().split("=", ", ", "}")
                for(i:Int in 0..10){
                    //Log.d("@@@@datata@@@@@", split[i*2 + 1])
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartData_soil_humi)
                }

                LineChart(chartData_soil_humi,"soil_humi")

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val split = snapshot.value.toString().split("=", ", ", "}")
                for(i:Int in 0..10){
                    //Log.d("@@@@datata@@@@@", split[i*2 + 1])
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartData_temp)
                }

                LineChart(chartData_temp,"temp")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        myRef3.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val split = snapshot.value.toString().split("=", ", ", "}")
                for(i:Int in 0..10){
                    //Log.d("@@@@datata@@@@@", split[i*2 + 1])
                    addChartItem("$i", split[i*2+1].toDouble()+0 , chartData_light)
                }

                LineChart(chartData_light,"light")

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        //차트 만들기 값을 가져오려면? 파이어베이스의  값을 불러오고, 변수를 addchartitem메서드에 인자로 넣기
        /*chartData_humidity.clear()
        addChartItem("12.5", 7.9,chartData_humidity)
        addChartItem("13.00", 8.2,chartData_humidity)
        addChartItem("13.5", 8.3,chartData_humidity)
        addChartItem("14.00", 8.5,chartData_humidity)
        addChartItem("14.5", 7.3,chartData_humidity)*/

        //LineChart(chartData_humidity,"humidity")

        /*addChartItem("12.5", 5.9,chartData_soil_humi)
        addChartItem("13.00", 6.2,chartData_soil_humi)
        addChartItem("13.5", 7.3,chartData_soil_humi)
        addChartItem("14.00", 8.5,chartData_soil_humi)
        addChartItem("14.5", 3.3,chartData_soil_humi)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_soil_humi,"soil_humi")

        addChartItem("12.5", 5.9,chartData_temp)
        addChartItem("13.00", 5.3,chartData_temp)
        addChartItem("13.5", 8.6,chartData_temp)
        addChartItem("14.00", 6.2,chartData_temp)
        addChartItem("14.5", 3.9,chartData_temp)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_temp,"temp")

        addChartItem("12.5", 7.0,chartData_light)
        addChartItem("13.00", 5.2,chartData_light)
        addChartItem("13.5", 5.3,chartData_light)
        addChartItem("14.00", 8.5,chartData_light)
        addChartItem("14.5", 7.3,chartData_light)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_light,"light")*/

    }

    private fun addChartItem(lableitem: String, dataitem: Double, chartData: ArrayList<ChartData>) {
        val item = ChartData()
        item.lableData = lableitem
        item.lineData = dataitem
        chartData.add(item)
    }

    private fun LineChart(chartData: ArrayList<ChartData>, name : String) {
        if(name == "humidity")
            lineChart = findViewById(R.id.linechart_humidity)
        else if(name == "soil_humi")
            lineChart = findViewById(R.id.linechart_soil_humi)
        else if(name == "light")
            lineChart = findViewById(R.id.linechart_light)
        else if(name == "temp")
            lineChart = findViewById(R.id.linechart_temp)
        //  lineChart = findViewById(R.id.linechart_temp)

        val entries = mutableListOf<Entry>()  //차트 데이터 셋에 담겨질 데이터

        for (item in chartData) {
            entries.add(Entry(item.lableData.replace(("[^\\d.]").toRegex(), "").toFloat(), item.lineData.toFloat()))
        }

        //LineDataSet 선언
        val lineDataSet: LineDataSet
        lineDataSet = LineDataSet(entries, name)
        if(name == "humidity")
            lineDataSet.color = Color.BLUE  //LineChart에서 Line Color 설정
        else if(name == "soil_humi")
            lineDataSet.color = Color.RED
        else if(name == "light")
            lineDataSet.color = Color.GREEN
        else if(name == "temp")
            lineDataSet.color = Color.YELLOW

        lineDataSet.setCircleColor(Color.DKGRAY)  // LineChart에서 Line Circle Color 설정
        lineDataSet.setCircleHoleColor(Color.DKGRAY) // LineChart에서 Line Hole Circle Color 설정

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet) // add the data sets

        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        lineChart.setData(data)
        lineChart.setDescription(null); //차트에서 Description 설정 삭제
        //XAxis.XAxisPosition.BOTTOM // 라벨 위치 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.invalidate();
    }

}
data class ChartData(
    var lableData: String = "",
    var valData: Double = 0.0,
    var lineData: Double = 0.0
)