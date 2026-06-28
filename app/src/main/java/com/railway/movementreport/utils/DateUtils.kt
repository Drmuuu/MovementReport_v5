package com.railway.movementreport.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    const val DB_DATE_FORMAT = "yyyy-MM-dd"
    const val DISPLAY_DATE_FORMAT = "dd/MM/yyyy"
    const val MONTH_YEAR_FORMAT = "yyyy-MM"
    const val DISPLAY_MONTH_FORMAT = "MMMM yyyy"
    const val EXPORT_DATE_FORMAT = "dd-MM-yyyy"

    fun getTodayDbFormat(): String {
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    fun getTodayDisplayFormat(): String {
        return SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    fun dbToDisplay(dbDate: String): String {
        return try {
            val inFmt = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            val outFmt = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            outFmt.format(inFmt.parse(dbDate)!!)
        } catch (e: Exception) {
            dbDate
        }
    }

    fun displayToDb(displayDate: String): String {
        return try {
            val inFmt = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            val outFmt = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            outFmt.format(inFmt.parse(displayDate)!!)
        } catch (e: Exception) {
            displayDate
        }
    }

    fun getMonthYearFromDb(dbDate: String): String {
        return try {
            dbDate.substring(0, 7)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDisplayMonthYear(monthYear: String): String {
        return try {
            val inFmt = SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault())
            val outFmt = SimpleDateFormat(DISPLAY_MONTH_FORMAT, Locale.getDefault())
            outFmt.format(inFmt.parse(monthYear)!!)
        } catch (e: Exception) {
            monthYear
        }
    }

    fun getCurrentMonthYear(): String {
        return SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault()).format(Date())
    }

    fun getExportDate(): String {
        return SimpleDateFormat(EXPORT_DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    fun getMonthName(monthYear: String): String {
        return try {
            val inFmt = SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault())
            val outFmt = SimpleDateFormat("MMMM", Locale.ENGLISH)
            outFmt.format(inFmt.parse(monthYear)!!)
        } catch (e: Exception) {
            ""
        }
    }

    fun getYear(monthYear: String): String {
        return try {
            monthYear.substring(0, 4)
        } catch (e: Exception) {
            ""
        }
    }

    fun dbToDisplayShort(dbDate: String): String {
        return try {
            val inFmt = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            val outFmt = SimpleDateFormat("d-M-yy", Locale.getDefault())
            outFmt.format(inFmt.parse(dbDate)!!)
        } catch (e: Exception) {
            dbDate
        }
    }

    fun addDays(dbDate: String, days: Int): String {
        return try {
            val fmt = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.time = fmt.parse(dbDate)!!
            cal.add(Calendar.DAY_OF_MONTH, days)
            fmt.format(cal.time)
        } catch (e: Exception) {
            dbDate
        }
    }
}