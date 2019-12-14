package com.brins.locksmith.viewmodel.main


import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brins.locksmith.data.DateBean
import com.brins.locksmith.viewmodel.passport.PassportRepository
import java.text.SimpleDateFormat
import java.util.*


class MainViewModel(private val repository: PassportRepository) : ViewModel() {

    val mDateLiveData = MutableLiveData<DateBean>()
    private lateinit var mDate: DateBean

    init {
        getDate()
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate() {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")// HH:mm:ss
//获取当前时间
        val date = Date(System.currentTimeMillis())
        val dateString = simpleDateFormat.format(date)

        val calendar = Calendar.getInstance()
        val weekString: String = getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))
        mDate = DateBean("" , "$dateString $weekString")
        mDateLiveData.value = mDate
    }


    private fun getDayOfWeek(day: Int): String {
        return when (day) {
            1 -> "Sun"
            2 -> "Mon"
            3 -> "Tue"
            4 -> "Wed"
            5 -> "Thu"
            6 -> "Fri"
            7 -> "Sat"
            else -> ""

        }
    }

}