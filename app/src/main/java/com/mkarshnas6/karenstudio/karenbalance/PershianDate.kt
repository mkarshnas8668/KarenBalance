import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PersianDate {

    companion object {

        fun getPersianDateToday(): String {
            return getCurrentShamsiDate()
        }

        private class SolarCalendar {

            var strWeekDay: String = ""
            var strMonth: String = ""

            var date: Int = 0
            var month: Int = 0
            var year: Int = 0

            constructor(miladiDate: Date) {
                calcSolarCalendar(miladiDate)
            }

            constructor(dateString: String) {
                val format = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                val miladiDate: Date? = try {
                    format.parse(dateString)
                } catch (e: java.text.ParseException) {
                    e.printStackTrace()
                    null
                }
                calcSolarCalendar(miladiDate ?: Date())
            }

            private fun calcSolarCalendar(miladiDate: Date) {
                val buf1 = arrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
                val buf2 = arrayOf(0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335)

                val miladiYear = miladiDate.year + 1900
                val miladiMonth = miladiDate.month + 1
                val miladiDay = miladiDate.date
                val weekDay = miladiDate.day

                val isLeapYear = miladiYear % 4 == 0
                val dayOfYear =
                    if (isLeapYear) buf2[miladiMonth - 1] + miladiDay else buf1[miladiMonth - 1] + miladiDay
                val leapOffset = if (isLeapYear && miladiYear >= 1996) 79 else 80

                if (dayOfYear > leapOffset) {
                    var solarDayOfYear = dayOfYear - leapOffset
                    if (solarDayOfYear <= 186) {
                        month = (solarDayOfYear - 1) / 31 + 1
                        date = (solarDayOfYear - 1) % 31 + 1
                    } else {
                        solarDayOfYear -= 186
                        month = (solarDayOfYear - 1) / 30 + 7
                        date = (solarDayOfYear - 1) % 30 + 1
                    }
                    year = miladiYear - 621
                } else {
                    val offset = if (miladiYear % 4 == 1) 10 else 9
                    val solarDayOfYear = dayOfYear + offset
                    month = (solarDayOfYear - 1) / 30 + 10
                    date = (solarDayOfYear - 1) % 30 + 1
                    year = miladiYear - 622
                }

                date += 1
                if ((month <= 6 && date > 31) || (month > 6 && date > 30)) {
                    date = 1
                    month += 1
                }
                if (month > 12) {
                    month = 1
                    year += 1
                }

                strMonth = when (month) {
                    1 -> "فروردین"
                    2 -> "اردیبهشت"
                    3 -> "خرداد"
                    4 -> "تیر"
                    5 -> "مرداد"
                    6 -> "شهریور"
                    7 -> "مهر"
                    8 -> "آبان"
                    9 -> "آذر"
                    10 -> "دی"
                    11 -> "بهمن"
                    12 -> "اسفند"
                    else -> ""
                }

                strWeekDay = when (weekDay) {
                    0 -> "یکشنبه"
                    1 -> "دوشنبه"
                    2 -> "سه‌شنبه"
                    3 -> "چهارشنبه"
                    4 -> "پنج‌شنبه"
                    5 -> "جمعه"
                    6 -> "شنبه"
                    else -> ""
                }
            }
        }

        fun getCurrentShamsiDate(): String {
            val solarCalendar = SolarCalendar(Date())
            return "${solarCalendar.year}/${
                solarCalendar.month.toString().padStart(2, '0')
            }/${solarCalendar.date.toString().padStart(2, '0')}"
        }

        fun getShamsiDateFromMiladi(dateString: String): String {
            val solarCalendar = SolarCalendar(dateString)
            return "${solarCalendar.year}/${
                solarCalendar.month.toString().padStart(2, '0')
            }/${solarCalendar.date.toString().padStart(2, '0')}"
        }
    }
}

