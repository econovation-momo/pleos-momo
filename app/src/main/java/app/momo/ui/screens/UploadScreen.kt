package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.momo.ui.overlays.*

private val ROUTES = listOf(15, 45, 120)
private val VISIBILITY = listOf("전체" to BotStatus.REVIEW, "링크" to BotStatus.LINK, "비공개" to BotStatus.PRIVATE)

/** S8 마켓에 올리기. 대상은 app.uploadBotId, 없으면 방금 만든 봇, 그것도 없으면 내 봇 첫 번째. */
@Composable
fun UploadScreen(app: AppState, car: CarState) {
    val bot = (app.uploadBotId ?: Graph.creator.state.value.bot?.id ?: app.myBotIds.firstOrNull())
        ?.let { id -> app.bots.firstOrNull { it.id == id } }
        ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("올릴 봇이 없어요", fontSize = 19.sp) }

    // 화면을 떠나기 전까지의 입력값. 봇이 바뀌면 처음부터.
    var one by remember(bot.id) { mutableStateOf(bot.oneLiner.take(30)) }
    var mins by remember(bot.id) { mutableStateOf(bot.minutes.toSet()) }
    var vis by remember(bot.id) { mutableIntStateOf(if (bot.status == BotStatus.LINK) 1 else 0) }
    var rating by remember(bot.id) { mutableStateOf(bot.rating) }
    var tested by remember(bot.id) { mutableStateOf(true) }
    var guide by remember(bot.id) { mutableStateOf(false) }
    var sent by remember(bot.id) { mutableStateOf<BotStatus?>(null) }

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

    Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        // 왼쪽: 제목과 마켓 카드 미리보기
        Row(Modifier.width(376.dp).fillMaxHeight()) {
            IconBox("←") { AppStore.go(Screen.MY) }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.fillMaxHeight()) {
                Text("마켓에 올리기", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("말로 고치거나 자동 값을 그대로 써요 · 정차 중에만", fontSize = 14.sp, color = Momo.Sub)
                Spacer(Modifier.height(24.dp))
                Cover(bot, Modifier.fillMaxWidth().aspectRatio(1.34f).clip(Round))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bot.title, Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Badge("미리보기")
                }
                Text(one, fontSize = 14.sp, maxLines = 1)
                Text(
                    listOf(bot.creator, bot.playersLabel, mins.sorted().joinToString("·") { minLabel(it) }, rating.label).joinToString(" · "),
                    fontSize = 13.sp, color = Momo.Sub,
                )
                Spacer(Modifier.weight(1f))
                Panel(Modifier.fillMaxWidth()) {
                    Text("창작 적립", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("다른 사람이 이 봇으로 이야기한 시간만큼 블루멤버스 포인트가 쌓여요(제휴 전제)", fontSize = 13.sp, color = Momo.Sub)
                }
            }
        }

        // 가운데: 봇 정보
        Panel(Modifier.weight(1f).fillMaxHeight()) {
            Row {
                Text("봇 정보", Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("자동 생성됨", fontSize = 13.sp, color = Momo.Sub)
            }
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Field("한 줄 소개", "${one.length}/30 · 눌러서 고쳐요") {
                    BasicTextField(
                        one, { one = it.take(30) },
                        Modifier.width(280.dp).background(Momo.Bg, RoundedCornerShape(11.dp)).padding(11.dp),
                        singleLine = true, textStyle = TextStyle(fontSize = 14.sp, color = Momo.Ink),
                    )
                }
                Field("지원 경로 시간", "고른 시간마다 시작 설정을 만들어요") {
                    ROUTES.forEach { m ->
                        Pill(minLabel(m), m in mins, off = Momo.Bg) { mins = if (m in mins && mins.size > 1) mins - m else mins + m }
                    }
                }
                Field("공개 범위", listOf("전체 공개는 AI 검수 후 마켓에 보여요", "링크 공개는 가족·친구만 봐요", "나만 봐요")[vis]) {
                    VISIBILITY.forEachIndexed { i, (label, _) -> Pill(label, vis == i, off = Momo.Bg) { vis = i } }
                }
                Field("대상 등급", if (rating == Rating.KIDS) "키즈는 AI 검수 후 키즈 모드에 노출" else "키즈 모드에서는 보이지 않아요") {
                    Rating.entries.forEach { r -> Pill(r.label, rating == r, off = Momo.Bg) { rating = r } }
                }
                Field("참여 인원 · 역할", "${bot.playersLabel} · ${bot.roles.joinToString(", ")}") {}
                Field("태그", "마켓 검색에 쓰여요") {
                    bot.tags.forEach { Pill("#$it", false, off = Momo.Bg) }
                }
            }
        }

        // 오른쪽: 올리기 전 확인
        Column(Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Panel(Modifier.fillMaxWidth().weight(1f)) {
                Row {
                    Text("올리기 전 확인", Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("${listOf(tested, guide).count { it }} / 2", fontSize = 13.sp, color = Momo.Sub)
                }
                HorizontalDivider(Modifier.padding(top = 8.dp), color = Momo.Line)
                CheckRow("테스트 플레이 1회", if (tested) "완료" else "한 번 들어 보고 올려요", tested) { tested = !tested }
                HorizontalDivider(color = Momo.Line)
                CheckRow("창작 가이드 · 저작권", if (guide) "확인했어요" else "눌러서 확인해 주세요", guide) { guide = !guide }
                Spacer(Modifier.weight(1f))
                if (sent != null) Text(
                    when (sent) {
                        BotStatus.REVIEW -> "AI 안전 검수 중 · 통과하면 마켓에 공개돼요"
                        BotStatus.LINK -> "링크 공개로 저장했어요 · 가족 공유로 보낼 수 있어요"
                        else -> "비공개로 저장했어요"
                    },
                    Modifier.fillMaxWidth().background(Momo.Bg, RoundedCornerShape(11.dp)).padding(12.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                )
                else Text("검수는 보통 몇 분 안에 끝나요. 통과하면 마켓에 공개되고, 마이페이지에서 상태를 볼 수 있어요.", fontSize = 13.sp, color = Momo.Sub)
            }
            val ready = tested && guide
            Row(
                Modifier.fillMaxWidth().clip(Round).background(if (sent != null || ready) Momo.Ink else Momo.Sub)
                    .clickable { if (sent != null) AppStore.go(Screen.MY) else if (ready) submit() }.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(if (sent != null) "올리기 완료" else if (vis == 0) "AI 안전 검수" else "저장", fontSize = 13.sp, color = Momo.Line)
                    Text(
                        if (sent != null) "마이페이지에서 상태 보기" else if (vis == 0) "검수 요청하고 올리기" else "이대로 저장하기",
                        fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Momo.OnAccent,
                    )
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(Momo.Bg), contentAlignment = Alignment.Center) { Text("›", fontSize = 27.sp) }
            }
        }
    }
}

@Composable
private fun Field(title: String, sub: String, trailing: @Composable RowScope.() -> Unit) {
    HorizontalDivider(Modifier.padding(top = 8.dp), color = Momo.Line)
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, fontSize = 13.sp, color = Momo.Sub)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), content = trailing)
    }
}
