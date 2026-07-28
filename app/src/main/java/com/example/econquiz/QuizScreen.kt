package com.example.econquiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

/**
 * 퀴즈 화면 (두 가지 모드로 사용)
 * - lockMode = true  : 잠금화면 위에서 사용. 정답을 맞혀야 "잠금 해제" 버튼이 나온다.
 * - lockMode = false : 연습 모드. 계속 다음 문제를 풀 수 있고 "홈으로" 버튼이 있다.
 *
 * v2 변경점: 문제를 풀 때마다 StatsManager에 기록을 저장하고,
 *            화면을 열 때 오늘의 기록을 불러와서 이어서 표시한다.
 */
@Composable
fun QuizScreen(
    lockMode: Boolean = false,
    onUnlock: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current

    val questions = remember { QuizRepository.questions.shuffled() }
    var questionIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // 오늘의 기록을 저장소에서 불러와서 시작 (화면을 껐다 켜도 이어짐)
    var solvedCount by remember { mutableStateOf(StatsManager.todaySolved(context)) }
    var correctCount by remember { mutableStateOf(StatsManager.todayCorrect(context)) }

    val question = questions[questionIndex % questions.size]
    val answered = selectedIndex != null

    val navy = Color(0xFF14283C)
    val gold = Color(0xFFF2B138)
    val green = Color(0xFF2E9E6B)
    val red = Color(0xFFD9534F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(navy)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Text("EconQuiz", color = gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "오늘 ${solvedCount}문제 · 정답 ${correctCount}개",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
        if (!lockMode) {
            TextButton(onClick = onExit) {
                Text("← 홈으로", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))

        // 카테고리 뱃지
        Text(
            text = question.category,
            color = navy,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(gold, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        )
        Spacer(Modifier.height(20.dp))

        // 문제
        Text(
            text = question.question,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 32.sp
        )
        Spacer(Modifier.height(32.dp))

        // 보기 4개
        question.choices.forEachIndexed { index, choice ->
            val buttonColor = when {
                !answered -> Color.White.copy(alpha = 0.10f)
                index == question.answer -> green
                index == selectedIndex -> red
                else -> Color.White.copy(alpha = 0.05f)
            }
            Button(
                onClick = {
                    if (!answered) {
                        selectedIndex = index
                        val isCorrect = index == question.answer
                        solvedCount++
                        if (isCorrect) correctCount++
                        // ★ 폰 저장소에 기록 (앱을 꺼도 남는다)
                        StatsManager.record(context, isCorrect)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .height(58.dp)
            ) {
                Text(choice, color = Color.White, fontSize = 16.sp)
            }
        }

        // 답을 고른 후: 해설 + 상황별 버튼
        if (answered) {
            val isCorrect = selectedIndex == question.answer
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isCorrect) "정답입니다! 🎉" else "아쉬워요 😅",
                    color = if (isCorrect) green else red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = question.explanation,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
            Spacer(Modifier.height(24.dp))

            if (lockMode) {
                if (isCorrect) {
                    Button(
                        onClick = onUnlock,
                        colors = ButtonDefaults.buttonColors(containerColor = gold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Text("잠금 해제 🔓", color = navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedIndex = null
                            questionIndex++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Text("다른 문제 도전 💪", color = Color.White, fontSize = 16.sp)
                    }
                    TextButton(onClick = onUnlock) {
                        Text("그냥 넘어가기", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            } else {
                Button(
                    onClick = {
                        selectedIndex = null
                        questionIndex++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                ) {
                    Text("다음 문제", color = navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}