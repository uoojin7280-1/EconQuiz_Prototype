package com.example.econquiz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * 백그라운드에서 계속 살아있으면서 "화면 꺼짐" 순간을 감시하는 서비스.
 *
 * 왜 화면이 '꺼질 때' 퀴즈를 띄울까?
 * → 화면이 꺼지는 순간 퀴즈 화면을 미리 최상단에 올려두면,
 *   사용자가 화면을 켰을 때 즉시 퀴즈가 보이기 때문 (잠금화면 앱들의 표준 방식)
 *
 * "포그라운드 서비스"란?
 * → 안드로이드는 일반 백그라운드 작업을 금방 죽여버린다.
 *   알림을 하나 띄워두는 조건으로 계속 살아있을 수 있는 특수한 서비스가 포그라운드 서비스.
 */
class LockScreenService : Service() {

    companion object {
        var isRunning = false          // 홈 화면에서 켜짐/꺼짐 상태 표시용
        const val CHANNEL_ID = "lockscreen_quiz"
    }

    // 화면 꺼짐(ACTION_SCREEN_OFF) 방송을 수신하는 리시버
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                val quizIntent = Intent(context, QuizLockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(quizIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startAsForeground()
        // 화면 꺼짐 이벤트 구독 시작
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    private fun startAsForeground() {
        // 알림 채널 생성 (Android 8.0 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "잠금화면 퀴즈",
                NotificationManager.IMPORTANCE_LOW  // 소리/진동 없는 조용한 알림
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EconQuiz 실행 중")
            .setContentText("화면을 켜면 경제 퀴즈가 나타납니다")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 시스템이 서비스를 죽여도 다시 살려달라는 요청
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}