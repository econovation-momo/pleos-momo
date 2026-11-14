package app.momo.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.ui.Momo

private val REASONS = listOf(
    "부적절한 내용" to "무섭거나 불쾌한 장면, 욕설·차별",
    "아이에게 위험" to "위험한 행동을 따라 하게 만드는 말",
    "저작권 침해" to "다른 작품의 캐릭터나 내용을 베낌",
    "기타" to "말로 이유를 남길 수 있어요",
)

/** R1 신고 시트. AI·사용자 제작 콘텐츠라 신고 기능이 필수다. 대상은 지금 고른 봇(app.bot). */
@Composable
fun ReportSheet(app: AppState) {
    val bot = app.bot
    var target by remember { mutableIntStateOf(0) }
    var reason by remember { mutableStateOf<Int?>(null) }
    var sent by remember { mutableStateOf(false) }

    Modal(520.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBox("⚑")
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (sent) "신고가 접수됐어요" else "무엇이 문제였나요?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${bot.title} · ${bot.creator}", fontSize = 13.sp, color = Momo.Sub)
            }
            IconBox("✕") { AppStore.close() }
        }
        Spacer(Modifier.height(16.dp))
        if (!sent) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("대상", fontSize = 14.sp, color = Momo.Sub)
                listOf("봇 전체", "방금 들은 대사").forEachIndexed { i, l -> Pill(l, target == i) { target = i } }
            }
            Spacer(Modifier.height(12.dp))
            Column(Modifier.background(Momo.Surface, Round).padding(horizontal = 18.dp)) {
                REASONS.forEachIndexed { i, (title, sub) ->
                    if (i > 0) HorizontalDivider(color = Momo.Line)
                    CheckRow(title, sub, reason == i) { reason = i }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("운영팀과 AI 검수가 함께 확인해요", Modifier.weight(1f), fontSize = 13.sp, color = Momo.Sub)
                MainButton("취소", dark = false) { AppStore.close() }
                MainButton("신고 보내기", enabled = reason != null) { sent = true }
            }
        } else {
            Text(
                "「${REASONS[reason ?: 0].first}」 사유로 ${if (target == 0) "봇 전체" else "방금 들은 대사"}를 신고했어요.\n확인 결과는 마이페이지 알림으로 알려 드려요.",
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(20.dp))
            MainButton("닫기", Modifier.fillMaxWidth()) { AppStore.close() }
        }
    }
}
