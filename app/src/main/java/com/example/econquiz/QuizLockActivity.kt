package com.example.econquiz

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

/**
 * 잠금화면 "위에" 표시되는 퀴즈 화면.
 * - setShowWhenLocked(true): 이 액티비티가 잠금화면보다 위에 뜨게 하는 핵심 한 줄
 * - 뒤로가기 버튼은 무시해서 퀴즈를 건너뛸 수 없게 함
 * - 정답을 맞히면 finish()로 스스로 닫힘 → 원래 잠금화면(또는 홈)으로 이동
 */
class QuizLockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위에 표시되도록 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        // 뒤로가기 버튼 막기 (퀴즈를 풀거나 '그냥 넘어가기'로만 나갈 수 있음)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 아무것도 하지 않음 = 뒤로가기 무시
            }
        })

        setContent {
            MaterialTheme {
                QuizScreen(
                    lockMode = true,
                    onUnlock = { finish() }  // 정답 시 이 화면을 닫는다
                )
            }
        }
    }
}