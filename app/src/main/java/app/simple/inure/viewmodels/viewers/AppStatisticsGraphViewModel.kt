package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.usage.UsageEvents
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.UsageStatsViewModel
import app.simple.inure.models.AppUsageModel
import app.simple.inure.models.DataUsage
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.UsageInterval
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AppStatisticsGraphViewModel(application: Application, private val packageInfo: PackageInfo) : UsageStatsViewModel(application) {

    private val packageStats: MutableLiveData<PackageStats> by lazy {
        MutableLiveData<PackageStats>().also {
            loadStatsData()
        }
    }

    private val barChartData: MutableLiveData<ArrayList<BarEntry>> by lazy {
        MutableLiveData<ArrayList<BarEntry>>()
    }

    private val pieChartData: MutableLiveData<ArrayList<PieEntry>> by lazy {
        MutableLiveData<ArrayList<PieEntry>>()
    }

    fun getPackageStats(): LiveData<PackageStats> {
        return packageStats
    }

    fun getChartData(): LiveData<ArrayList<BarEntry>> {
        return barChartData
    }

    fun getPieChartData(): LiveData<ArrayList<PieEntry>> {
        return pieChartData
    }

    private fun loadStatsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(getUsageEvents()) {
                    if (this.appUsage?.size?.isNotZero() == true) {
                        packageStats.postValue(this)
                        loadPieChartData(this)
                        loadChartData(this)
                    } else {
                        warning.postValue(getString(R.string.usage_data_does_not_exist_for_this_app))
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getUsageEvents(): PackageStats {
        val packageStats = PackageStats()
        packageStats.packageInfo = packageInfo
        packageStats.appUsage = arrayListOf()

        val interval = UsageInterval.getTimeInterval()
        val events: UsageEvents = usageStatsManager.queryEvents(interval.startTime, interval.endTime)
        val event = UsageEvents.Event()

        var startTime: Long
        var endTime: Long
        var skipNew = false
        var iteration = 0

        while (events.hasNextEvent()) {
            if (!skipNew) events.getNextEvent(event)

            var eventTime = event.timeStamp

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) { // App is visible (foreground)
                startTime = eventTime

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    eventTime = event.timeStamp

                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        skipNew = true
                        break
                    } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        endTime = eventTime
                        skipNew = false
                        if (packageInfo.packageName.equals(event.packageName)) {
                            val time = endTime - startTime + 1
                            packageStats.appUsage?.add(iteration, AppUsageModel(startTime, time, endTime))
                            packageStats.launchCount = iteration.plus(1)
                            packageStats.totalTimeUsed += time
                            packageStats.lastUsageTime = endTime

                            iteration++
                        }
                        break
                    }
                }
            }
        }

        packageStats.appUsage?.reverse()
        getDataUsage(packageStats)

        return packageStats
    }

    private fun getDataUsage(packageStats: PackageStats) {
        var mobileData = SparseArrayCompat<DataUsage>()
        var wifiData = SparseArrayCompat<DataUsage>()

        kotlin.runCatching {
            mobileData = getMobileData(StatisticsPreferences.getInterval())
        }
        kotlin.runCatching {
            wifiData = getWifiData(StatisticsPreferences.getInterval())
        }

        val uid: Int = packageStats.packageInfo?.applicationInfo?.uid!!

        if (mobileData.containsKey(uid)) {
            packageStats.mobileData = mobileData[uid]
        } else {
            packageStats.mobileData = DataUsage.EMPTY
        }

        if (wifiData.containsKey(uid)) {
            packageStats.wifiData = wifiData[uid]
        } else {
            packageStats.wifiData = DataUsage.EMPTY
        }
    }

    private fun loadChartData(packageStats: PackageStats) {
        viewModelScope.launch(Dispatchers.Default) {
            Log.d("TAG", "loadChartData: ${packageStats.appUsage?.size}")
            val barEntries = arrayListOf(
                    BarEntry(0f, 0f),
                    BarEntry(1f, 0f),
                    BarEntry(2f, 0f),
                    BarEntry(3f, 0f),
                    BarEntry(4f, 0f),
                    BarEntry(5f, 0f),
                    BarEntry(6f, 0f))

            packageStats.appUsage?.forEach {
                when {
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 6 -> {
                        barEntries[0].y += it.startTime
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 5 -> {
                        barEntries[1].y += it.startTime
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 4 -> {
                        barEntries[2].y += it.startTime
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 3 -> {
                        barEntries[3].y += it.startTime
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 2 -> {
                        barEntries[4].y += it.startTime
                    }
                    CalendarUtils.isYesterday(Date(it.date)) -> {
                        barEntries[5].y += it.startTime
                    }
                    CalendarUtils.isToday(it.date) -> {
                        barEntries[6].y += it.startTime
                    }
                }

                barChartData.postValue(barEntries)
            }
        }
    }

    private fun loadPieChartData(packageStats: PackageStats) {
        viewModelScope.launch(Dispatchers.Default) {
            val context = applicationContext()
            val pieEntries = arrayListOf(
                    PieEntry(0f, ""),
                    PieEntry(0f, ""),
                    PieEntry(0f, ""),
                    PieEntry(0f, ""),
                    PieEntry(0f, ""),
                    PieEntry(0f, ""),
                    PieEntry(0f, "")
            )

            packageStats.appUsage?.forEach {
                when {
                    CalendarUtils.isToday(it.date) -> { // Today
                        Log.d("TAG", "loadPieChartData: Today 0 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[0].value + it.startTime, context.getString(R.string.today))
                            pieEntries.remove(pieEntries[0])
                            pieEntries.add(0, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getString(R.string.today)))
                        }
                    }
                    CalendarUtils.isYesterday(Date(it.date)) -> { // Yesterday
                        Log.d("TAG", "loadPieChartData: Yesterday 1 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[1].value + it.startTime, context.getString(R.string.yesterday))
                            pieEntries.remove(pieEntries[1])
                            pieEntries.add(1, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getString(R.string.yesterday)))
                        }
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 2 -> { // 2 days ago
                        Log.d("TAG", "loadPieChartData: 2 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[2].value + it.startTime, context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date)))
                            pieEntries.remove(pieEntries[2])
                            pieEntries.add(2, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))))
                        }
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 3 -> { // 3 days ago
                        Log.d("TAG", "loadPieChartData: 3 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[3].value + it.startTime, context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date)))
                            pieEntries.remove(pieEntries[3])
                            pieEntries.add(3, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))))
                        }
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 4 -> { // 4 days ago
                        Log.d("TAG", "loadPieChartData: 4 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[4].value + it.startTime, context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date)))
                            pieEntries.remove(pieEntries[4])
                            pieEntries.add(4, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))))
                        }
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 5 -> { // 5 days ago
                        Log.d("TAG", "loadPieChartData: 5 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[5].value + it.startTime, context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date)))
                            pieEntries.remove(pieEntries[5])
                            pieEntries.add(5, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))))
                        }
                    }
                    CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) == 6 -> { // 6 days ago
                        Log.d("TAG", "loadPieChartData: 6 ${context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))} : ${CalendarUtils.getWeekNumberFromDate(it.date)}")
                        try {
                            val pieEntry = PieEntry(pieEntries[6].value + it.startTime, context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date)))
                            pieEntries.remove(pieEntries[6])
                            pieEntries.add(6, pieEntry)
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getWeekName(CalendarUtils.getWeekNumberFromDate(it.date))))
                        }
                    }
                    //                    else -> {
                    //                        try {
                    //                            val pieEntry = PieEntry(pieEntries[7].value + it.startTime, context.getString(R.string.rest_of_the_week))
                    //                            pieEntries.remove(pieEntries[7])
                    //                            pieEntries.add(2, pieEntry)
                    //                        } catch (e: java.lang.IndexOutOfBoundsException) {
                    //                            pieEntries.add(PieEntry(it.startTime.toFloat(), context.getString(R.string.rest_of_the_week)))
                    //                        }
                    //                    }
                }
            }

            pieChartData.postValue(pieEntries)
        }
    }

    //    private fun calculateDailyAverage(pieEntries: List<PieEntry>) {
    //        var tally = 0
    //        var total = 0L
    //
    //        for (i in pieEntries.indices) {
    //            if (pieEntries[i].value.isNotZero()) {
    //                tally++
    //                total += pieEntries[i].value.toLong()
    //            }
    //        }
    //
    //        val average = total / pieEntries.size
    //
    //        dailyAverage.postValue(average)
    //    }

    private fun Context.getWeekName(weekNumber: Int): String {
        return when (weekNumber) {
            7 -> getString(R.string.sun)
            1 -> getString(R.string.mon)
            2 -> getString(R.string.tue)
            3 -> getString(R.string.wed)
            4 -> getString(R.string.thu)
            5 -> getString(R.string.fri)
            6 -> getString(R.string.sat)
            else -> ""
        }
    }

    @Suppress("unused")
    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = getInstalledApps()

            var size = 0L

            for (i in apps.indices) {
                size += apps[i].applicationInfo.sourceDir.getDirectoryLength()
            }
        }
    }
}
