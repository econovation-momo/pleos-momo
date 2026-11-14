package app.momo.ui.overlays

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph
import app.momo.core.AppStore
import app.momo.core.BotStatus
import app.momo.core.Overlay
import app.momo.core.Screen
import app.momo.core.StoryBot
import app.momo.ui.Momo

// 마켓·마이페이지·올리기 화면과 모달이 같이 쓰는 작은 조각들이다.
// Figma 시안(1280×720)을 에뮬레이터(160dpi) 앱 창에 옮긴다. 앱 창이 약 1256×850dp라 시안 수치를 1:1로 쓴다.

val Round = RoundedCornerShape(16.dp)

/** 회색 바탕 카드 */
@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.background(Momo.Surface, Round).padding(18.dp), content = content)

/** 고르는 칩. 선택되면 검정, 아니면 [off] 색 */
@Composable
fun Pill(label: String, on: Boolean, modifier: Modifier = Modifier, off: Color = Momo.Surface, onClick: (() -> Unit)? = null) = Text(
    label,
    modifier.clip(RoundedCornerShape(12.dp)).background(if (on) Momo.Ink else off)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 16.dp, vertical = 9.dp),
    color = if (on) Momo.OnAccent else Momo.Ink, fontSize = 14.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
)

/** 큰 버튼. dark=false면 테두리만 */
@Composable
fun MainButton(label: String, modifier: Modifier = Modifier, dark: Boolean = true, enabled: Boolean = true, onClick: () -> Unit) = Box(
    modifier.height(48.dp).clip(RoundedCornerShape(24.dp))
        .background(if (!dark) Color.Transparent else if (enabled) Momo.Ink else Momo.Line)
        .then(if (dark) Modifier else Modifier.border(1.dp, Momo.Ink, RoundedCornerShape(24.dp)))
        .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center,
) {
    Text(label, color = if (dark) Momo.OnAccent else Momo.Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
}

/** 아이콘 대신 글자 하나를 넣는 네모 버튼(✕, ←, ✎ 등) */
@Composable
fun IconBox(glyph: String, modifier: Modifier = Modifier, bg: Color = Momo.Surface, onClick: (() -> Unit)? = null) = Box(
    modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(bg)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    contentAlignment = Alignment.Center,
) { Text(glyph, fontSize = 18.sp, color = Momo.Ink) }

/** 작은 배지. dark면 검정 바탕 */
@Composable
fun Badge(text: String, dark: Boolean = false, modifier: Modifier = Modifier) = Text(
    text,
    modifier.background(if (dark) Momo.Ink else Momo.Line, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
    color = if (dark) Momo.OnAccent else Momo.Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium,
)

/** 동그란 체크 표시 */
@Composable
fun CheckDot(on: Boolean) = Box(
    Modifier.size(24.dp).clip(CircleShape).background(if (on) Momo.Ink else Color.Transparent)
        .then(if (on) Modifier else Modifier.border(1.dp, Momo.Sub, CircleShape)),
    contentAlignment = Alignment.Center,
) { if (on) Text("✓", color = Momo.OnAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

/** 제목·설명·체크 한 줄 */
@Composable
fun CheckRow(title: String, sub: String, on: Boolean, lead: String? = null, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    if (lead != null) { Text(lead, fontSize = 18.sp); Spacer(Modifier.width(12.dp)) }
    Column(Modifier.weight(1f)) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(sub, fontSize = 13.sp, color = Momo.Sub)
    }
    CheckDot(on)
}

/** 화면을 덮는 모달 틀. 바깥을 누르면 [onDismiss] */
@Composable
fun Modal(width: Dp, onDismiss: () -> Unit = AppStore::close, content: @Composable ColumnScope.() -> Unit) = Box(
    Modifier.fillMaxSize().background(Momo.Scrim)
        .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
    contentAlignment = Alignment.Center,
) {
    Column(
        Modifier.width(width).clip(RoundedCornerShape(24.dp)).background(Momo.Bg)
            .clickable(remember { MutableInteractionSource() }, indication = null) {}.padding(24.dp),
        content = content,
    )
}

/** 봇 표지. 그림이 없으면 캐릭터 이름 첫 글자 */
@Composable
fun Cover(bot: StoryBot?, modifier: Modifier = Modifier) {
    val res = bot?.cover
    if (res != null) Image(painterResource(res), bot.title, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Momo.Line), contentAlignment = Alignment.Center) {
        Text(bot?.character?.name?.take(1) ?: "?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Momo.Sub)
    }
}

fun minLabel(m: Int) = if (m >= 60 && m % 60 == 0) "${m / 60}시간" else "${m}분"
val StoryBot.minsLabel get() = minutes.joinToString("·") { minLabel(it) }
val StoryBot.playersLabel get() = if (maxPlayers <= 1) "1명" else "1~${maxPlayers}명"
fun count(n: Int) = "%,d".format(n)

/** 받침이 있으면 "이", 없으면 "가". 「제목」이 나타나요 같은 문장에 쓴다. 숫자로 끝나면 읽는 소리(영·일·삼…)로 판단한다. */
fun iGa(word: String): String {
    val c = word.lastOrNull { it in '가'..'힣' || it.isDigit() } ?: return "이"
    val batchim = if (c.isDigit()) c in "013678" else (c - '가') % 28 != 0
    return if (batchim) "이" else "가"
}

/** 말로 만들기 오버레이를 처음부터 연다 */
fun openCreate() { Graph.creator.reset(); AppStore.open(Overlay.CREATE) }

/** 이 봇을 마켓에 올리기 화면으로 */
fun goUpload(id: String) = AppStore.update { it.copy(uploadBotId = id, screen = Screen.UPLOAD, overlay = Overlay.NONE) }

/** 공유 모달은 지금 고른 봇(app.bot)을 보낸다. 화면은 그대로 둔다. */
fun openShare(id: String) = AppStore.update { it.copy(botId = id, overlay = Overlay.SHARE) }

/** 마켓 '+ 내 봇 올리기': 아직 비공개인 내 봇이 있으면 그 봇을 올리고, 없으면 말로 새로 만든다 */
fun uploadOrCreate(myBots: List<StoryBot>) =
    myBots.firstOrNull { it.status == BotStatus.PRIVATE }?.let { goUpload(it.id) } ?: openCreate()
