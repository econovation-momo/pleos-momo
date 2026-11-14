package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.momo.Graph
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Overlay
import app.momo.core.Screen
import app.momo.core.StoryBot
import app.momo.ui.Momo
import app.momo.voice.VoiceStatus
import app.momo.ui.components.*
import kotlinx.coroutines.launch

/** S3 봇 상세(구도 B, 21:7): 꽉 찬 배경 위 왼쪽에 봇 소개를 쌓고, 오른쪽 유리 카드 하나에 캐릭터·역할·만든 사람을 담는다. */
@Composable
fun DetailScreen(app: AppState, car: CarState) {
    val bot = app.bot
    val min = rideMin(app, car)
    val start = bot.startFor(min)
    val scope = rememberCoroutineScope()
    val voice by Graph.voice.status.collectAsStateWithLifecycle()
    val sample = bot.exampleDialogues.firstOrNull()?.substringAfter(": ") ?: bot.oneLiner

    HeroScreen(bot, side = { InfoPanel(app, bot, sample, Modifier.width(380.dp)) }) {
        HeroHeader(
            kicker = "STORY BOT · ${bot.character.name}",
            title = bot.title,
            subtitle = bot.oneLiner,
            meta = botMeta(bot, min),
            body = bot.description,
        )
        Spacer(Modifier.height(26.dp))
        StartBar(bot, start.minutes, destLabel(app, car))
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillButton("이 봇으로 출발 준비", height = 64.dp, fontSize = 22.sp) { AppStore.go(Screen.PLAYERS) }
            Spacer(Modifier.width(12.dp))
            // 목소리 미리듣기: 예시 대사 한 줄을 캐릭터 목소리로 말한다
            GhostButton(if (voice == VoiceStatus.SPEAKING) "말하는 중…" else "목소리 미리듣기") {
                scope.launch { Graph.voice.speak(sample) }
            }
        }
    }
}

/** 시작 설정 막대: Figma의 진행 막대 자리. 경로 길이에 맞춰 고른 시작 설정까지 채운다. */
@Composable
private fun StartBar(bot: StoryBot, picked: Int, dest: String) {
    val starts = bot.starts.map { it.minutes }.sorted()
    val idx = starts.indexOf(picked)
    Column(Modifier.width(400.dp)) {
        Text("시작 설정 ${minLabel(picked)} · $dest", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ProgressBar((idx + 1f) / starts.size, Color.White, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row {
            starts.forEach { m ->
                Text(
                    minLabel(m), Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 15.sp,
                    color = if (m == picked) Color.White else Color.White.copy(alpha = .55f),
                    fontWeight = if (m == picked) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

/** 오른쪽 위 유리 카드: 캐릭터, 맡을 역할, 만든 사람·팔로우, 신고 */
@Composable
private fun InfoPanel(app: AppState, bot: StoryBot, sample: String, modifier: Modifier) {
    val following = bot.creator in app.following
    GlassCard(modifier) {
        Text("${bot.character.name} · ${bot.character.voice}", color = Momo.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("“$sample”", color = Momo.Ink, fontSize = 17.sp, lineHeight = 24.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(14.dp))
        Text("맡을 역할 · 탑승자마다 하나씩", color = Momo.Sub, fontSize = 15.sp)
        Text(bot.roles.joinToString("  ·  "), color = Momo.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("만든 사람 ${bot.creator} · ${"%,d".format(bot.plays)}회", color = Momo.Sub, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (following) "팔로잉" else "팔로우 +",
                Modifier.clickable { AppStore.update { it.copy(following = if (following) it.following - bot.creator else it.following + bot.creator) } }
                    .padding(6.dp),
                color = Momo.Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            )
            Text("신고", Modifier.clickable { AppStore.open(Overlay.REPORT) }.padding(6.dp), color = Momo.Sub, fontSize = 16.sp)
        }
    }
}
