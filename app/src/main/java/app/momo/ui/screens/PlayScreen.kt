package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.momo.Graph
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Phase
import app.momo.core.Screen
import app.momo.engine.PlayState
import app.momo.ui.Momo
import app.momo.ui.components.*
import app.momo.voice.VoiceStatus

/**
 * S5 플레이: Figma 21:52(주행) / 21:88(정차). 화면은 PlayState만 보고 그린다.
 * 주행 중에는 대표 이미지를 흐린 배경으로 고정하고(장면이 바뀌어도 교체하지 않음), 애니메이션을 쓰지 않는다.
 * 정차하면 현재 장면 그림을 선명하게 보여 주지만 이야기는 멈추지 않는다.
 */
@Composable
fun PlayScreen(app: AppState, car: CarState) {
    val play by Graph.engine.state.collectAsStateWithLifecycle()
    val voiceNow by Graph.voice.status.collectAsStateWithLifecycle()
    val bot = play.bot

    LaunchedEffect(play.ending) {
        play.ending?.let { AppStore.addEndingCard(it); AppStore.go(Screen.ENDING) }
    }

    if (bot == null) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("이야기가 아직 시작되지 않았어요", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            PillButton("홈으로", onImage = false) { AppStore.go(Screen.HOME) }
        }
        return
    }

    val driving = car.driving
    val ink = if (driving) Momo.Ink else Color.White
    // 음성 상태가 엔진 상태보다 먼저 바뀌므로 음성 쪽을 우선한다(입력 후 0.25초 안에 표시)
    val status = if (voiceNow != VoiceStatus.IDLE) voiceNow else play.status

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (driving) {
            Cover(bot, Modifier.fillMaxSize().blur(24.dp, BlurredEdgeTreatment.Rectangle))
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = .62f)))
        } else {
            Cover(bot, Modifier.fillMaxSize(), image = bot.scenes.takeIf { it.isNotEmpty() }?.let { it[play.sceneIndex % it.size] } ?: bot.cover)
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Black.copy(alpha = .45f), .5f to Color.Black.copy(alpha = .2f), 1f to Color.Black.copy(alpha = .5f)),
                ),
            )
        }

        ChapterBar(play, ink, Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 20.dp))

        // 가운데 대사 묶음: Figma처럼 왼쪽에서 22% 지점부터
        Column(Modifier.fillMaxWidth().padding(start = maxWidth * .216f, end = 80.dp, top = 100.dp)) {
            Text(
                play.prevLine, Modifier.blur(if (driving) 3.dp else 0.dp), color = ink.copy(alpha = .3f),
                fontSize = 23.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(43.dp))
            Text(bot.character.name, color = ink.copy(alpha = if (driving) .55f else 1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(13.dp))
            // 주행 중 규정: 120자·3줄 이하(엔진이 120자 안으로 자르고, 화면은 3줄에서 자른다)
            Text(
                play.line, color = ink, fontSize = 35.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold,
                maxLines = 3, overflow = TextOverflow.Ellipsis, style = KoBreak,
            )
        }

        TurnBadge(play, status, ink, driving, Modifier.align(Alignment.BottomStart).padding(28.dp))

        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 31.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!driving) DemoChips(play)
            play.lastHeard?.let { HeardCard(it, play.lastHeardBy, driving) }
        }
    }
}

/** 상단 장 진행 막대(5칸)와 장 제목, 오른쪽 아래에 단계·남은 시간 */
@Composable
private fun ChapterBar(play: PlayState, ink: Color, modifier: Modifier) {
    val chapters = play.bot?.chapters?.takeIf { it.isNotEmpty() } ?: Phase.entries.dropLast(1).map { it.label }
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            chapters.forEachIndexed { i, title ->
                Column(Modifier.weight(1f)) {
                    ProgressBar(if (i <= play.chapter) 1f else 0f, ink, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(7.dp))
                    Text(title, color = ink.copy(alpha = if (i <= play.chapter) 1f else .5f), fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            "${play.phase.label} · 도착까지 ${play.remainSec / 60}분 · 장면 ${play.sceneIndex + 1}/${play.sceneCount}",
            Modifier.align(Alignment.End), color = ink.copy(alpha = .7f), fontSize = 15.sp,
        )
    }
}

/** 누구 차례인지 + 마이크. 듣는 중이면 마이크 원을 채운다. */
@Composable
private fun TurnBadge(play: PlayState, status: VoiceStatus, ink: Color, driving: Boolean, modifier: Modifier) {
    val listening = status == VoiceStatus.LISTENING
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(56.dp).background(if (listening) Momo.Ink else ink.copy(alpha = .15f), CircleShape),
            Alignment.Center,
        ) { MicGlyph(if (listening) Color.White else ink, 24.dp) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                play.turnOf?.let { "${it.name} · ${it.role ?: "듣기만"} 차례" } ?: "모두 함께",
                color = ink, fontSize = 19.sp, fontWeight = FontWeight.Bold,
            )
            Text(
                when (status) {
                    VoiceStatus.LISTENING -> "듣고 있어요"
                    VoiceStatus.THINKING -> "생각 중…"
                    VoiceStatus.SPEAKING -> "${play.bot?.character?.name ?: ""} 말하는 중"
                    VoiceStatus.IDLE -> if (driving) "이름을 부르면 대답해요" else "정차 중 · 이야기는 계속돼요"
                },
                color = ink.copy(alpha = .7f), fontSize = 15.sp,
            )
        }
    }
}

/** 하단 '방금 들은 말 · 이름' 카드 */
@Composable
private fun HeardCard(text: String, by: String?, driving: Boolean) {
    val fg = if (driving) Momo.Ink else Color.White
    Column(
        Modifier.width(600.dp)
            // 유리 카드 기준: 주행 중에는 흰색 85%, 정차 중에는 장면 그림이 비치도록 옅게(21:88)
            .background(Color.White.copy(alpha = if (driving) .85f else .22f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = .9f), RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 15.dp),
    ) {
        Text("방금 들은 말" + (by?.let { " · $it" } ?: ""), color = fg.copy(alpha = .7f), fontSize = 15.sp)
        Text("“$text”", color = fg, fontSize = 25.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** 정차 중에만 보이는 시연 입력. 상황에 맞는 예시 발화를 음성 입력처럼 넣는다. */
@Composable
private fun DemoChips(play: PlayState) {
    val name = play.bot?.character?.name ?: ""
    val samples = when (play.phase) {
        Phase.OPENING -> listOf("좋아, 가 보자!", "${name}, 무슨 일이야?", "조금 무서워…")
        Phase.RISING -> listOf("저쪽으로 가 보자!", "내가 도와줄게!", "수리수리 마수리!")
        Phase.CLIMAX -> listOf("하나, 둘, 셋!", "다 같이 힘내자!")
        Phase.ENDING, Phase.DONE -> listOf("고마워, 또 만나!", "다음엔 어디 갈까?")
    }
    Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("시연 입력", color = Color.White.copy(alpha = .7f), fontSize = 15.sp)
        samples.forEach { s -> GhostButton(s, fontSize = 15.sp) { Graph.voice.inject(s) } }
        if (play.phase < Phase.ENDING) GhostButton("결말로 건너뛰기", fontSize = 15.sp) { Graph.engine.skipToEnding() }
    }
}
