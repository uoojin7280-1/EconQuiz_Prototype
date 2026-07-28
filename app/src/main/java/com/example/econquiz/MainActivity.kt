package com.example.econquiz

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var serviceRunning by remember { mutableStateOf(LockScreenService.isRunning) }
    var practiceMode by remember { mutableStateOf(false) }

    if (practiceMode) {
        QuizScreen(lockMode = false, onExit = { practiceMode = false })
        return
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 허용 여부와 관계없이 진행 */ }

    val navy = Color(0xFF14283C)
    val gold = Color(0xFFF2B138)
    val red = Color(0xFFD9534F)

    // 통계 읽기 (연습 모드에서 돌아올 때마다 자동으로 새로 읽힌다)
    val todaySolved = StatsManager.todaySolved(context)
    val todayCorrect = StatsManager.todayCorrect(context)
    val totalSolved = StatsManager.totalSolved(context)
    val totalCorrect = StatsManager.totalCorrect(context)
    val todayRate = if (todaySolved > 0) todayCorrect * 100 / todaySolved else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(navy)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        Text("EconQuiz", color = gold, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "화면을 켤 때마다 경제 상식이 쌓입니다",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(32.dp))

        // ===== 오늘의 통계 카드 =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "오늘의 기록",
                color = gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = "${todaySolved}", label = "푼 문제")
                StatItem(value = "${todayCorrect}", label = "정답")
                StatItem(value = "${todayRate}%", label = "정답률")
            }
            Text(
                "지금까지 총 ${totalSolved}문제를 풀고 ${totalCorrect}문제를 맞혔어요",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "잠금화면 퀴즈를 켜기 전에\n아래 두 가지를 먼저 허용해 주세요",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(16.dp))

        HomeButton("1. '다른 앱 위에 표시' 허용") {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }

        HomeButton("2. 배터리 최적화 제외") {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (!serviceRunning) {
                    if (!Settings.canDrawOverlays(context)) {
                        Toast.makeText(
                            context,
                            "먼저 1번 '다른 앱 위에 표시'를 허용해 주세요",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        val serviceIntent = Intent(context, LockScreenService::class.java)
                        if (Build.VERSION.SDK_INT >= 26) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        serviceRunning = true
                        Toast.makeText(context, "이제 화면을 껐다 켜보세요! 🔒", Toast.LENGTH_LONG).show()
                    }
                } else {
                    context.stopService(Intent(context, LockScreenService::class.java))
                    serviceRunning = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (serviceRunning) red else gold
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                text = if (serviceRunning) "잠금화면 퀴즈 끄기" else "잠금화면 퀴즈 켜기 🔒",
                color = if (serviceRunning) Color.White else navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        HomeButton("연습 모드로 퀴즈 풀기") { practiceMode = true }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
fun HomeButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(56.dp)
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
    }
}