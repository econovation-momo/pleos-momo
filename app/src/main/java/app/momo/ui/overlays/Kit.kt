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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph
import app.momo.core.AppStore
import app.momo.core.BotStatus
import app.momo.core.Overlay
import app.momo.core.Screen
import app.momo.core.StoryBot
import app.momo.ui.Momo

// 마켓·마이페이지·올리기 화면과 모달이 같이 쓰는 조각들이다. 표지·유리 카드 모양은 ui/components/MomoKit.kt와 같은 값을 쓴다.
// 양식은 Figma 상단 4장(21:122, 21:7, 21:52, 21:88)을 따른다:
// 이미지가 꽉 찬 카드 + 아래 그라데이션 위 흰 글씨, 알약 버튼, 얇은 진행 막대, 반투명 흰 유리 카드, 굵기는 Bold·Regular 두 단계.
// 에뮬레이터 앱 창이 약 1256×850dp(160dpi)라 시안(1280×720) 수치를 1:1로 쓴다.

val Round = RoundedCornerShape(20.dp)
private val PillShape = RoundedCornerShape(50)
const val DIM = 0.4f // 선택되지 않은 것은 흐리게
val OnImageSub = Color.White.copy(alpha = 0.75f)

/** 필터·선택 칩: 선택은 검정 알약, 비선택은 연회색 알약 */
@Composable
fun Pill(label: String, on: Boolean, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) = Text(
    label,
    modifier.clip(PillShape).background(if (on) Momo.Ink else Momo.Surface)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 18.dp, vertical = 10.dp),
    color = if (on) Momo.OnAccent else Momo.Ink, fontSize = 15.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
)

/**
 * 주 버튼(알약). 흰 바탕에서는 검정 알약에 흰 글자, onImage면 흰 알약에 검정 글자('이어서 시청' 버튼 모양).
 * light는 흰 바탕의 보조 버튼(연회색 알약). icon을 주면 앞에 검정(또는 흰) 원 안에 그린다.
 */
@Composable
fun PillButton(
    label: String, modifier: Modifier = Modifier, icon: String? = null, onImage: Boolean = false, light: Boolean = false,
    enabled: Boolean = true, height: Dp = 52.dp, onClick: () -> Unit,
) {
    val bg = when { onImage -> Color.White; light -> Momo.Surface; else -> Momo.Ink }
    val fg = if (onImage || light) Momo.Ink else Momo.OnAccent
    val small = height < 48.dp
    val side = if (small) 14.dp else 24.dp
    Row(
        modifier.height(height).alpha(if (enabled) 1f else DIM).clip(PillShape).background(bg)
            .clickable(enabled = enabled, onClick = onClick).padding(start = if (icon != null) 8.dp else side, end = side),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Box(Modifier.size(height - 16.dp).clip(CircleShape).background(fg), contentAlignment = Alignment.Center) {
                Text(icon, color = bg, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
        }
        Text(label, color = fg, fontSize = if (small) 14.sp else 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** 동그란 글자 버튼(✕, ← 등). 장식 없이 연한 원만 둔다. */
@Composable
fun RoundButton(glyph: String, modifier: Modifier = Modifier, onClick: () -> Unit) = Box(
    modifier.size(44.dp).clip(CircleShape).background(Momo.Ink.copy(alpha = 0.06f)).clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) { Text(glyph, fontSize = 18.sp, color = Momo.Ink) }

/** "15·45분 | 1~2명 | 키즈" 같은 메타 줄 */
@Composable
fun Meta(parts: List<String>, color: Color = Momo.Sub, size: TextUnit = 13.sp, modifier: Modifier = Modifier) =
    Text(parts.filter { it.isNotEmpty() }.joinToString("  |  "), modifier, color = color, fontSize = size, maxLines = 1, overflow = TextOverflow.Ellipsis)

/** 얇은 진행 막대(4dp, 둥근 끝) */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, track: Color = Momo.Ink.copy(alpha = 0.15f), fill: Color = Momo.Ink) =
    Box(modifier.height(4.dp).clip(PillShape).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(PillShape).background(fill))
    }

/** 반투명 흰 유리 카드(Figma 21:52 '방금 들은 말' 카드) */
@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier.clip(Round).background(Color.White.copy(alpha = 0.9f))
        .border(1.dp, Color.White.copy(alpha = 0.9f), Round).padding(28.dp),
    content = content,
)

/** 어두운 스크림 위 유리 카드 모달. 바깥을 누르면 [onDismiss] */
@Composable
fun Modal(width: Dp, onDismiss: () -> Unit = AppStore::close, content: @Composable ColumnScope.() -> Unit) = Box(
    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)) // 뒤 화면이 유리 카드에 비치지 않게 기본 스크림보다 조금 어둡게
        .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
    contentAlignment = Alignment.Center,
) {
    GlassCard(Modifier.width(width).clickable(remember { MutableInteractionSource() }, indication = null) {}, content = content)
}

/** 모달 머리: 큰 제목, 메타 줄, 닫기 */
@Composable
fun ModalHeader(title: String, meta: List<String>) = Row(verticalAlignment = Alignment.Top) {
    Column(Modifier.weight(1f)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Meta(meta, size = 14.sp)
    }
    RoundButton("✕") { AppStore.close() }
}

/** 동그란 체크. 선택은 검정 원 */
@Composable
fun CheckDot(on: Boolean) = Box(
    Modifier.size(26.dp).clip(CircleShape).background(if (on) Momo.Ink else Color.Transparent)
        .then(if (on) Modifier else Modifier.border(1.5.dp, Momo.Ink, CircleShape)),
    contentAlignment = Alignment.Center,
) { if (on) Text("✓", color = Momo.OnAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

/** 제목·설명·체크 한 줄. 고르지 않은 줄은 흐리게([dim]) */
@Composable
fun CheckRow(title: String, sub: String, on: Boolean, dim: Boolean = !on, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clickable(onClick = onClick).alpha(if (dim) DIM else 1f).padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Column(Modifier.weight(1f)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(sub, fontSize = 14.sp, color = Momo.Sub)
    }
    CheckDot(on)
}

/** 봇 표지. 그림이 없으면 어두운 그라데이션 */
@Composable
fun Cover(bot: StoryBot?, modifier: Modifier = Modifier) {
    // 핵심 화면과 같은 표지 규칙(이미지 없으면 같은 그라데이션)을 쓰도록 MomoKit에 맡긴다
    if (bot != null) app.momo.ui.components.Cover(bot, modifier)
    else Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF3A3F4B), Color(0xFF111111)))))
}

/** 이미지 위 작은 흰 반투명 알약(재생 수, 상태 등) */
@Composable
fun ImageTag(text: String, dark: Boolean = false, modifier: Modifier = Modifier) = Text(
    text,
    modifier.clip(PillShape).background(if (dark) Momo.Ink else Color.White.copy(alpha = 0.85f)).padding(horizontal = 10.dp, vertical = 4.dp),
    color = if (dark) Momo.OnAccent else Momo.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold,
)

/**
 * 세로형 표지 카드(Figma 21:122 동화 선택 카드). 표지가 꽉 차고, 아래 그라데이션 위에 [content]를 흰 글씨로 얹는다.
 * [top]은 왼쪽 위에 겹친다.
 */
@Composable
fun CoverCard(
    bot: StoryBot?, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null,
    top: @Composable RowScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit,
) = Box(modifier.clip(Round).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
    Cover(bot, Modifier.matchParentSize())
    Box(Modifier.matchParentSize().background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f))))
    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), content = top)
    Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp), content = content)
}

/** 카드 안 흰 제목 */
@Composable
fun CardTitle(text: String, size: TextUnit = 19.sp) =
    Text(text, color = Color.White, fontSize = size, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)

/** 카드 안 흰 두 줄 소개 */
@Composable
fun CardBody(text: String) =
    Text(text, color = Color.White, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)

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
