package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.BotStatus
import app.momo.core.Rating
import app.momo.core.Screen
import app.momo.core.StartSetting
import app.momo.ui.Momo
import app.momo.ui.components.*
import app.momo.ui.overlays.*

private val ROUTES = listOf(15, 45, 120)
private val VISIBILITY = listOf("전체" to BotStatus.REVIEW, "링크" to BotStatus.LINK, "비공개" to BotStatus.PRIVATE)

/**
 * S8 마켓에 올리기: Figma 21:7 히어로. 배경은 그 봇의 표지, 왼쪽은 마켓 카드처럼 보이는 제목·메타·소개와 올리기 버튼,
 * 오른쪽 유리 카드에 입력 항목을 알약 선택지로 담는다. 대상은 app.uploadBotId, 없으면 방금 만든 봇, 그것도 없으면 내 봇 첫 번째.
 */
@Composable
fun UploadScreen(app: AppState, car: CarState) {
    val bot = (app.uploadBotId ?: Graph.creator.state.value.bot?.id ?: app.myBotIds.firstOrNull())
        ?.let { id -> app.bots.firstOrNull { it.id == id } }
        ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("올릴 봇이 없어요", fontSize = 24.sp) }

    // 화면을 떠나기 전까지의 입력값. 봇이 바뀌면 처음부터.
    var one by remember(bot.id) { mutableStateOf(bot.oneLiner.take(30)) }
    var mins by remember(bot.id) { mutableStateOf(bot.minutes.toSet()) }
    var vis by remember(bot.id) { mutableIntStateOf(if (bot.status == BotStatus.LINK) 1 else 0) }
    var rating by remember(bot.id) { mutableStateOf(bot.rating) }
    var tested by remember(bot.id) { mutableStateOf(true) }
    var guide by remember(bot.id) { mutableStateOf(false) }
    var sent by remember(bot.id) { mutableStateOf<BotStatus?>(null) }
    val done = listOf(tested, guide).count { it }

    fun submit() {
        val status = VISIBILITY[vis].second
        AppStore.update { s ->
            s.copy(bots = s.bots.map { b ->
                if (b.id != bot.id) b
                // 새로 고른 경로 시간은 가장 가까운 기존 시작 설정을 빌려 쓴다. 실제 생성은 엔진이 맡는다.
                else b.copy(oneLiner = one, rating = rating, starts = mins.sorted().map { m -> b.starts.firstOrNull { it.minutes == m } ?: StartSetting(m, b.startFor(m).prologue) })
            })
        }
        AppStore.setStatus(bot.id, status)
        sent = status
    }

    Hero(bot, Modifier.fillMaxSize().padding(horizontal = Gutter, vertical = 12.dp)) {
        // 왼쪽: 마켓 카드처럼 보이는 미리보기와 올리기 버튼
        Column(Modifier.align(Alignment.CenterStart).padding(start = 40.dp).width(520.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundButton("←", onImage = true) { AppStore.go(Screen.MY) }
                Spacer(Modifier.width(12.dp))
                Text("마켓에 올리기", color = OnImageSub, fontSize = 17.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(bot.title, color = Color.White, fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, maxLines = 2, style = KoBreak)
            Spacer(Modifier.height(8.dp))
            MetaLine(listOf(mins.sorted().joinToString("·") { minLabel(it) }, bot.playersLabel, rating.label, "만든 사람 ${bot.creator}"), OnImageSub)
            Spacer(Modifier.height(14.dp))
            Text(one, color = Color.White, fontSize = 18.sp, lineHeight = 26.sp, maxLines = 2)
            Spacer(Modifier.height(24.dp))
            // 올리기 전 확인 두 가지를 진행 막대로
            ProgressBar(done / 2f, Color.White, Modifier.width(360.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                if (sent != null) "올리기 완료" else "올리기 전 확인 $done / 2 · 테스트 플레이, 창작 가이드",
                color = OnImageSub, fontSize = 14.sp,
            )
            Spacer(Modifier.height(20.dp))
            if (sent != null) {
                Text(
                    when (sent) {
                        BotStatus.REVIEW -> "AI 안전 검수 중이에요. 통과하면 마켓에 공개돼요."
                        BotStatus.LINK -> "링크 공개로 저장했어요. 가족 공유로 보낼 수 있어요."
                        else -> "비공개로 저장했어요."
                    },
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                PillButton("마이페이지에서 상태 보기") { AppStore.go(Screen.MY) }
            } else {
                val ready = tested && guide
                PillButton(
                    if (vis == 0) "검수 요청하고 올리기" else "이대로 저장하기", Modifier.alpha(if (ready) 1f else DIM),
                    icon = { Text("↑", color = it, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                ) { if (ready) submit() }
            }
        }

        // 오른쪽 유리 카드: 입력 항목
        GlassCard(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).width(430.dp).fillMaxHeight(0.92f)) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Field("한 줄 소개", "${one.length}/30") {
                    BasicTextField(
                        one, { one = it.take(30) },
                        Modifier.fillMaxWidth().background(Momo.Ink.copy(alpha = 0.06f), RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 11.dp),
                        singleLine = true, textStyle = TextStyle(fontSize = 15.sp, color = Momo.Ink),
                    )
                }
                Field("지원 경로 시간", "고른 시간마다 시작 설정을 만들어요") {
                    ROUTES.forEach { m -> Pill(minLabel(m), m in mins) { mins = if (m in mins && mins.size > 1) mins - m else mins + m } }
                }
                Field("공개 범위", listOf("AI 검수 후 마켓에 보여요", "가족·친구만 링크로 봐요", "나만 봐요")[vis]) {
                    VISIBILITY.forEachIndexed { i, (label, _) -> Pill(label, vis == i) { vis = i } }
                }
                Field("대상 등급", if (rating == Rating.KIDS) "AI 검수 후 키즈 모드에 노출" else "키즈 모드에서는 보이지 않아요") {
                    Rating.entries.forEach { r -> Pill(r.label, rating == r) { rating = r } }
                }
                Field("참여 인원 · 역할", "${bot.playersLabel}  |  ${bot.roles.joinToString(", ")}") {}
                Field("태그", bot.tags.joinToString("  ") { "#$it" }) {}
                Spacer(Modifier.height(4.dp))
                CheckRow("테스트 플레이 1회", if (tested) "완료했어요" else "한 번 들어 보고 올려요", tested) { tested = !tested }
                CheckRow("창작 가이드 · 저작권", if (guide) "확인했어요" else "눌러서 확인해 주세요", guide) { guide = !guide }
            }
        }
    }
}

/** 유리 카드 안 한 항목: 제목·설명, 그 아래 알약 선택지 */
@Composable
private fun Field(title: String, sub: String, content: @Composable RowScope.() -> Unit) = Column(Modifier.padding(vertical = 9.dp)) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(sub, fontSize = 13.sp, color = Momo.Sub)
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}
