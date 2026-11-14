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
import app.momo.voice.VoiceStatus
import app.momo.ui.components.*
import kotlinx.coroutines.launch

/** S3 봇 상세: Figma 21:7(꽉 찬 배경 이미지 + 왼쪽 텍스트) 위에 봇 정보를 얹는다. */
@Composable
fun DetailScreen(app: AppState, car: CarState) {
    val bot = app.bot
    val min = rideMin(app, car)
    val start = bot.startFor(min)
    val scope = rememberCoroutineScope()
    val voice by Graph.voice.status.collectAsStateWithLifecycle()
    val sample = bot.exampleDialogues.firstOrNull()?.substringAfter(": ") ?: bot.oneLiner

    Box(Modifier.fillMaxSize().padding(start = 19.dp, end = 19.dp, bottom = 19.dp).clip(RoundedCornerShape(3.dp))) {
        // 가로로 긴 장면 그림이 있으면 배경으로, 없으면 대표 이미지
        Cover(bot, Modifier.fillMaxSize(), image = bot.scenes.firstOrNull() ?: bot.cover)
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(0f to Color.Black.copy(alpha = .85f), .45f to Color.Black.copy(alpha = .45f), .75f to Color.Transparent),
            ),
        )

        Column(
            Modifier.fillMaxHeight().fillMaxWidth(.55f).padding(start = 48.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("STORY BOT · ${bot.character.name}", color = Color.White, fontSize = 21.sp)
            Spacer(Modifier.height(11.dp))
            Text(bot.title, color = Color.White, fontSize = 45.sp, lineHeight = 53.sp, fontWeight = FontWeight.Bold, maxLines = 2, style = KoBreak)
            Spacer(Modifier.height(5.dp))
            Text(bot.oneLiner, color = Color.White, fontSize = 21.sp, lineHeight = 28.sp)
            Spacer(Modifier.height(24.dp))
            Text(metaLine(bot, min), color = Color.White.copy(alpha = .7f), fontSize = 15.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                bot.description, color = Color.White, fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium,
                maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(27.dp))
            StartBar(bot, start.minutes, destLabel(app, car))
            Spacer(Modifier.height(29.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillButton("이 봇으로 출발 준비", dark = false, height = 61.dp, fontSize = 20.sp, leading = { PlayIcon() }) {
                    AppStore.go(Screen.PLAYERS)
                }
                Spacer(Modifier.width(13.dp))
                // 목소리 미리듣기: 예시 대사 한 줄을 캐릭터 목소리로 말한다
                Text(
                    if (voice == VoiceStatus.SPEAKING) "말하는 중…" else "목소리 미리듣기",
                    Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .18f))
                        .clickable { scope.launch { Graph.voice.speak(sample) } }.padding(horizontal = 21.dp, vertical = 16.dp),
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                )
            }
        }

        InfoPanel(app, bot, sample, Modifier.align(Alignment.TopEnd).padding(24.dp).width(430.dp))
    }
}

/** 시작 설정 막대: Figma의 진행 막대 자리. 경로 길이에 맞춰 고른 시작 설정까지 채운다. */
@Composable
private fun StartBar(bot: StoryBot, picked: Int, dest: String) {
    val starts = bot.starts.map { it.minutes }.sorted()
    val idx = starts.indexOf(picked)
    Column(Modifier.width(413.dp)) {
        Text("시작 설정 ${minLabel(picked)} · $dest", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(9.dp))
        Box(Modifier.fillMaxWidth().height(7.dp).background(Color.White.copy(alpha = .3f), RoundedCornerShape(50))) {
            Box(Modifier.fillMaxWidth((idx + 1f) / starts.size).fillMaxHeight().background(Color.White, RoundedCornerShape(50)))
        }
        Spacer(Modifier.height(7.dp))
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

/** 오른쪽 위 정보 카드: 캐릭터, 맡을 역할, 만든 사람·팔로우, 신고 */
@Composable
private fun InfoPanel(app: AppState, bot: StoryBot, sample: String, modifier: Modifier) {
    val following = bot.creator in app.following
    Column(modifier.background(Color.Black.copy(alpha = .55f), RoundedCornerShape(16.dp)).padding(19.dp)) {
        Text("${bot.character.name} · ${bot.character.voice}", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("“$sample”", color = Color.White.copy(alpha = .85f), fontSize = 18.sp, lineHeight = 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(15.dp))
        Text("맡을 역할 · 탑승자마다 하나씩", color = Color.White.copy(alpha = .6f), fontSize = 15.sp)
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            bot.roles.forEach { GlassChip(it) }
        }
        Spacer(Modifier.height(15.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("만든 사람 ${bot.creator} · ${"%,d".format(bot.plays)}회", color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            GlassChip(if (following) "팔로잉" else "팔로우 +", on = following) {
                AppStore.update { it.copy(following = if (following) it.following - bot.creator else it.following + bot.creator) }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "신고", Modifier.clickable { AppStore.open(Overlay.REPORT) }.padding(5.dp),
                color = Color.White.copy(alpha = .7f), fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun GlassChip(text: String, on: Boolean = false, onClick: (() -> Unit)? = null) = Text(
    text,
    Modifier.clip(RoundedCornerShape(8.dp)).background(if (on) Color.White else Color.White.copy(alpha = .18f))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 9.dp, vertical = 4.dp),
    color = if (on) Color.Black else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium,
)
