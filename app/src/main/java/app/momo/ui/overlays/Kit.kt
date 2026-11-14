package app.momo.ui.overlays

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import app.momo.ui.components.GlassCard
import app.momo.ui.components.Hero
import app.momo.ui.components.KoBreak
import app.momo.ui.components.MetaLine
import app.momo.ui.components.PillButton
import app.momo.ui.components.Scrim
import app.momo.ui.components.minLabel

// 마켓·마이페이지·올리기 화면과 모달이 같이 쓰는 조각 가운데 MomoKit(ui/components)에 없는 것만 둔다.
// 표지·히어로·알약 버튼·유리 카드·진행 막대는 MomoKit을 쓴다. 구도는 Figma 상단 4장(21:122 캐러셀, 21:7 히어로, 21:52·21:88 장면)을 따른다.

const val DIM = 0.4f // 선택되지 않은 것은 흐리게
val OnImageSub = Color.White.copy(alpha = 0.8f)
val PosterW = 313.dp // 21:122 세로 포스터 카드(가운데 카드는 330×500)
val PosterH = 472.dp

/** 필터·선택 칩: 선택은 검정 알약, 비선택은 옅은 알약. 흰 바탕과 유리 카드 위에서 같이 쓴다. */
@Composable
fun Pill(label: String, on: Boolean, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) = Text(
    label,
    modifier.background(if (on) Momo.Ink else Momo.Ink.copy(alpha = 0.06f), RoundedCornerShape(50))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 16.dp, vertical = 9.dp),
    color = if (on) Momo.OnAccent else Momo.Ink, fontSize = 15.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
)

/** 동그란 글자 버튼(✕, ← 등) */
@Composable
fun RoundButton(glyph: String, modifier: Modifier = Modifier, onImage: Boolean = false, onClick: () -> Unit) = Box(
    modifier.size(44.dp).background(if (onImage) Color.White.copy(alpha = 0.18f) else Momo.Ink.copy(alpha = 0.06f), CircleShape)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) { Text(glyph, fontSize = 18.sp, color = if (onImage) Color.White else Momo.Ink) }

/** 어두운 스크림 위 유리 카드 모달. 바깥을 누르면 [onDismiss] */
@Composable
fun Modal(width: Dp, onDismiss: () -> Unit = AppStore::close, content: @Composable ColumnScope.() -> Unit) = Box(
    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)) // 뒤 화면이 유리 카드에 비치지 않게 조금 어둡게
        .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
    contentAlignment = Alignment.Center,
) {
    GlassCard(Modifier.width(width).clickable(remember { MutableInteractionSource() }, indication = null) {}, alpha = 0.92f) {
        Column(Modifier.padding(vertical = 10.dp), content = content)
    }
}

/** 흰 바탕·유리 카드 위 주 버튼: MomoKit 검정 알약(아이콘 없음). 못 누르는 상태는 흐리게 */
@Composable
fun DarkPill(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    PillButton(text, modifier.alpha(if (enabled) 1f else DIM), onImage = false, icon = null, height = 52.dp, fontSize = 17.sp) { if (enabled) onClick() }

/** 모달 머리: 큰 제목, 메타 줄, 닫기 */
@Composable
fun ModalHeader(title: String, meta: List<String>) = Row(verticalAlignment = Alignment.Top) {
    Column(Modifier.weight(1f)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        MetaLine(meta, fontSize = 14.sp)
    }
    RoundButton("✕") { AppStore.close() }
}

/** 동그란 체크. 선택은 검정 원 */
@Composable
fun CheckDot(on: Boolean) = Box(
    Modifier.size(26.dp).background(if (on) Momo.Ink else Color.Transparent, CircleShape)
        .then(if (on) Modifier else Modifier.border(1.5.dp, Momo.Ink, CircleShape)),
    contentAlignment = Alignment.Center,
) { if (on) Text("✓", color = Momo.OnAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

/** 제목·설명·체크 한 줄. 고르지 않은 줄은 흐리게([dim]) */
@Composable
fun CheckRow(title: String, sub: String, on: Boolean, dim: Boolean = !on, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clickable(onClick = onClick).alpha(if (dim) DIM else 1f).padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Column(Modifier.weight(1f)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(sub, fontSize = 14.sp, color = Momo.Sub)
    }
    CheckDot(on)
}

/** 이미지 위 작은 알약(재생 수, 상태 등) */
@Composable
fun ImageTag(text: String, dark: Boolean = false, modifier: Modifier = Modifier) = Text(
    text,
    modifier.background(if (dark) Momo.Ink else Color.White.copy(alpha = 0.85f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
    color = if (dark) Momo.OnAccent else Momo.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold,
)

/**
 * 21:122 세로 포스터 카드. 표지가 꽉 차고 아래 그라데이션 위에 가운데 정렬로 흰 제목·메타·두 줄 소개를 얹는다.
 * bot이 null이면 어두운 그라데이션 카드(만들기·올리기·미발견 칸). focused=false면 흐리게 덮는다.
 */
@Composable
fun PosterCard(
    bot: StoryBot?, title: String, meta: List<String>, body: String?, modifier: Modifier,
    focused: Boolean = true, titleSize: Int = 30, onClick: (() -> Unit)? = null,
    top: @Composable RowScope.() -> Unit = {}, extra: @Composable ColumnScope.() -> Unit = {},
) = Box(modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
    if (bot != null) Hero(bot, Modifier.fillMaxSize(), scrim = Scrim.BOTTOM)
    else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF3A3F4B), Color(0xFF111111)))))
    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), content = top)
    Column(
        Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        extra()
        Text(
            title, color = Color.White, fontSize = titleSize.sp, lineHeight = (titleSize * 1.25f).sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 2, style = KoBreak,
        )
        if (meta.isNotEmpty()) { Spacer(Modifier.height(8.dp)); MetaLine(meta, OnImageSub, fontSize = 13.sp) }
        if (body != null) {
            Spacer(Modifier.height(14.dp))
            Text(body, Modifier.fillMaxWidth(), color = Color.White, fontSize = 14.sp, lineHeight = 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
    if (!focused) Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.55f)))
}

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
