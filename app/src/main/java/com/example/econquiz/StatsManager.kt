package com.example.econquiz

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 퀴즈 기록을 폰에 저장하는 관리자.
 * SharedPreferences = 안드로이드가 제공하는 작은 키-값 저장소.
 * 앱을 완전히 종료하거나 폰을 재부팅해도 기록이 남는다.
 *
 * 저장하는 값:
 * - today_date / today_solved / today_correct : 오늘의 기록 (날짜가 바뀌면 자동 리셋)
 * - total_solved / total_correct : 전체 누적 기록
 */
object StatsManager {

    private const val PREF_NAME = "econquiz_stats"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    // 날짜가 바뀌었으면 오늘 기록을 0으로 리셋
    private fun resetIfNewDay(context: Context) {
        val p = prefs(context)
        if (p.getString("today_date", "") != today()) {
            p.edit()
                .putString("today_date", today())
                .putInt("today_solved", 0)
                .putInt("today_correct", 0)
                .apply()
        }
    }

    // 문제를 하나 풀 때마다 호출해서 기록
    fun record(context: Context, correct: Boolean) {
        resetIfNewDay(context)
        val p = prefs(context)
        p.edit()
            .putInt("today_solved", p.getInt("today_solved", 0) + 1)
            .putInt("today_correct", p.getInt("today_correct", 0) + if (correct) 1 else 0)
            .putInt("total_solved", p.getInt("total_solved", 0) + 1)
            .putInt("total_correct", p.getInt("total_correct", 0) + if (correct) 1 else 0)
            .apply()
    }

    fun todaySolved(context: Context): Int {
        resetIfNewDay(context)
        return prefs(context).getInt("today_solved", 0)
    }

    fun todayCorrect(context: Context): Int {
        resetIfNewDay(context)
        return prefs(context).getInt("today_correct", 0)
    }

    fun totalSolved(context: Context): Int =
        prefs(context).getInt("total_solved", 0)

    fun totalCorrect(context: Context): Int =
        prefs(context).getInt("total_correct", 0)
}