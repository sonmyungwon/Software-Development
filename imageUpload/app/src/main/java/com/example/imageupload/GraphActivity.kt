package com.example.imageupload


import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class GraphActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    lateinit var lineChart: LineChart
    private val chartData_humidity = ArrayList<ChartData>()
    private val chartData_soil_humi = ArrayList<ChartData>()
    private val chartData_light = ArrayList<ChartData>()
    private val chartData_temp = ArrayList<ChartData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        // 서버에서 데이터 가져오기 (서버에서 가져온 데이터로 가정하고 직접 추가)
        chartData_humidity.clear()
        addChartItem("12.5", 7.9,chartData_humidity)
        addChartItem("13.00", 8.2,chartData_humidity)
        addChartItem("13.5", 8.3,chartData_humidity)
        addChartItem("14.00", 8.5,chartData_humidity)
        addChartItem("14.5", 7.3,chartData_humidity)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_humidity,"humidity")

        chartData_humidity.clear()
        addChartItem("12.5", 5.9,chartData_soil_humi)
        addChartItem("13.00", 6.2,chartData_soil_humi)
        addChartItem("13.5", 7.3,chartData_soil_humi)
        addChartItem("14.00", 8.5,chartData_soil_humi)
        addChartItem("14.5", 3.3,chartData_soil_humi)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_soil_humi,"soil_humi")

        chartData_humidity.clear()
        addChartItem("12.5", 5.9,chartData_temp)
        addChartItem("13.00", 5.3,chartData_temp)
        addChartItem("13.5", 8.6,chartData_temp)
        addChartItem("14.00", 6.2,chartData_temp)
        addChartItem("14.5", 3.9,chartData_temp)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_temp,"temp")

        chartData_humidity.clear()
        addChartItem("12.5", 7.0,chartData_light)
        addChartItem("13.00", 5.2,chartData_light)
        addChartItem("13.5", 5.3,chartData_light)
        addChartItem("14.00", 8.5,chartData_light)
        addChartItem("14.5", 7.3,chartData_light)

        // 그래프 그릴 자료 넘기기
        LineChart(chartData_light,"light")
    }

    private fun addChartItem(lableitem: String, dataitem: Double,chartData: ArrayList<ChartData>) {
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
