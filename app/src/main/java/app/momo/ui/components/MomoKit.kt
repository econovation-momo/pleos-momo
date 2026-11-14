package app.momo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.StoryBot
import app.momo.ui.Momo

/*
 * MOMO 공용 조각. 디자인 기준은 Figma 상단 4장(21:122 캐러셀, 21:7 상세, 21:52·21:88 플레이)이다.
 * - 이미지가 화면이나 카드를 꽉 채우고, 텍스트는 그 위 왼쪽에 얹는다(스크림 위 흰 글자).
 * - 굵기는 Bold와 Regular 두 단계만 쓰고, 회색은 메타 정보에만 쓴다.
 * - 주 버튼은 흰 알약 + 검정 원형 아이콘(이미지 위), 흰 바탕에서는 검정 알약으로 반전한다.
 * - 떠 있는 정보는 반투명 흰 유리 카드에 담는다('방금 들은 말' 카드 기준).
 */

/** 화면 좌우 기본 여백 */
val Gutter = 24.dp

// ---------- 계산 도우미 ----------

/** 이야기 길이(분): 내비 남은 시간이 우선이고, 없으면 '목적지 없이 시작'에서 고른 값, 그것도 없으면 30분. */
fun rideMin(app: AppState, car: CarState): Int = car.remainSec?.let { it / 60 } ?: app.rideMin ?: 30

/** 목표 장면 수. 엔진과 같은 기준(2분에 한 장면)으로 미리 보여 준다. */
fun scenesFor(min: Int) = (min / 2).coerceAtLeast(3)

fun destLabel(app: AppState, car: CarState) =
    car.destination?.let { "${it}까지 ${rideMin(app, car)}분" } ?: "목적지 없이 ${rideMin(app, car)}분 이야기"

fun minLabel(m: Int) = if (m >= 60 && m % 60 == 0) "${m / 60}시간" else "${m}분"

/** Figma 메타 줄 항목: 2026 | 키즈 | 45분 | 어드벤처 */
fun botMeta(bot: StoryBot, min: Int) = listOf("2026", bot.rating.label, "${bot.startFor(min).minutes}분", bot.genre)

/** 한국어 제목·대사를 어절 단위로 끊고 줄 길이를 고르게 맞춘다(API 33 이상에서 동작). */
val KoBreak = TextStyle(
    lineBreak = LineBreak(LineBreak.Strategy.Balanced, LineBreak.Strictness.Strict, LineBreak.WordBreak.Phrase),
)

// ---------- 이미지 ----------

/** 대표 이미지. 그림이 없는 봇은 어두운 그라데이션으로 대신한다. */
@Composable
fun Cover(bot: StoryBot, modifier: Modifier = Modifier, image: Int? = bot.cover) {
    if (image != null) Image(painterResource(image), bot.title, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF3A3F4B), Color(0xFF111111)))))
}

/** 스크림 방향: 왼쪽 텍스트용(상세 21:7) / 아래 텍스트용(캐러셀 카드 21:122) */
enum class Scrim { LEFT, BOTTOM }

/** 이미지 히어로: 이미지를 꽉 채우고, 텍스트가 놓일 쪽을 어둡게 깐다. */
@Composable
fun Hero(bot: StoryBot, modifier: Modifier = Modifier, image: Int? = bot.cover, scrim: Scrim = Scrim.LEFT, content: @Composable BoxScope.() -> Unit = {}) =
    Box(modifier) {
        Cover(bot, Modifier.fillMaxSize(), image)
        Box(
            Modifier.fillMaxSize().background(
                when (scrim) {
                    Scrim.LEFT -> Brush.horizontalGradient(0f to Color.Black.copy(alpha = .85f), .45f to Color.Black.copy(alpha = .45f), .75f to Color.Transparent)
                    Scrim.BOTTOM -> Brush.verticalGradient(.45f to Color.Transparent, 1f to Color.Black.copy(alpha = .9f))
                },
            ),
        )
        content()
    }

// ---------- 글자 ----------

/** 메타 줄: 항목 사이를 ' | '로 잇는 작은 글자 */
@Composable
fun MetaLine(parts: List<String>, color: Color = Momo.Sub, fontSize: TextUnit = 15.sp, modifier: Modifier = Modifier) =
    Text(parts.joinToString("  |  "), modifier, color = color, fontSize = fontSize)

// ---------- 버튼 ----------

/**
 * 주 버튼(알약). onImage=true면 이미지 위의 흰 알약 + 검정 아이콘('이어서 시청'),
 * false면 흰 바탕용으로 반전한 검정 알약 + 흰 아이콘.
 */
@Composable
fun PillButton(
    text: String,
    modifier: Modifier = Modifier,
    onImage: Boolean = true,
    icon: (@Composable (Color) -> Unit)? = { PlayGlyph(it) },
    fontSize: TextUnit = 20.sp,
    height: Dp = 60.dp,
    onClick: () -> Unit,
) {
    val bg = if (onImage) Color.White else Momo.Ink
    val fg = if (onImage) Momo.Ink else Color.White
    Row(
        modifier.height(height).clip(RoundedCornerShape(50)).background(bg).clickable(onClick = onClick)
            .padding(start = if (icon != null) 10.dp else 28.dp, end = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Box(Modifier.size(height - 20.dp).background(fg, CircleShape), Alignment.Center) { icon(bg) }
            Spacer(Modifier.width(12.dp))
        }
        Text(text, color = fg, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

/** 보조 버튼: 반투명 알약. 이미지 위에서는 흰 글자, 흰 바탕에서는 검정 글자. */
@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onImage: Boolean = true, fontSize: TextUnit = 18.sp, onClick: () -> Unit) = Text(
    text,
    modifier.clip(RoundedCornerShape(50)).background(if (onImage) Color.White.copy(alpha = .18f) else Momo.Ink.copy(alpha = .06f))
        .clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 14.dp),
    color = if (onImage) Color.White else Momo.Ink, fontSize = fontSize, fontWeight = FontWeight.Bold,
)

// ---------- 카드·막대 ----------

/** 반투명 흰 유리 카드: '방금 들은 말' 카드 양식. 모달·시트도 이 양식을 쓴다. */
@Composable
fun GlassCard(modifier: Modifier = Modifier, alpha: Float = .82f, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = alpha))
        .border(1.dp, Color.White.copy(alpha = .9f), RoundedCornerShape(20.dp)).padding(horizontal = 24.dp, vertical = 18.dp),
    content = content,
)

/** 얇은 진행 막대(4dp, 둥근 끝) */
@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) =
    Box(modifier.height(4.dp).background(color.copy(alpha = .3f), RoundedCornerShape(50))) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(color, RoundedCornerShape(50)))
    }

// ---------- 아이콘 ----------
// 아이콘 라이브러리를 들이지 않으려고 꼭 필요한 두 개만 직접 그린다.

/** 재생 삼각형 */
@Composable
fun PlayGlyph(color: Color, size: Dp = 14.dp) = Canvas(Modifier.size(size)) {
    val p = Path().apply {
        moveTo(this@Canvas.size.width * .15f, 0f)
        lineTo(this@Canvas.size.width, this@Canvas.size.height / 2)
        lineTo(this@Canvas.size.width * .15f, this@Canvas.size.height)
        close()
    }
    drawPath(p, color)
}

/** 마이크 */
@Composable
fun MicGlyph(color: Color = Color.White, size: Dp = 20.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    drawRoundRect(color, Offset(w * .35f, 0f), Size(w * .3f, h * .6f), CornerRadius(w * .15f))
    drawArc(color, 0f, 180f, false, Offset(w * .2f, h * .25f), Size(w * .6f, h * .5f), style = Stroke(w * .08f))
    drawLine(color, Offset(w / 2, h * .75f), Offset(w / 2, h * .95f), w * .08f)
}

/** 원 안에 이름 한 글자. 좌석 도식·배역 목록에서 같이 쓴다. 비어 있으면 점선 대신 흐린 원. */
@Composable
fun Avatar(name: String?, active: Boolean, size: Dp = 44.dp, modifier: Modifier = Modifier) = Box(
    modifier.size(size)
        .background(if (active) Momo.Ink else Color.White.copy(alpha = if (name == null) .5f else 1f), CircleShape)
        .border(2.dp, Color.White, CircleShape),
    Alignment.Center,
) {
    if (name != null) Text(
        if (name in setOf("엄마", "아빠")) name.take(1) else name.takeLast(1),
        color = if (active) Color.White else Momo.Ink, fontSize = (size.value * .36f).sp,
        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
    )
}

// =====================================================================
// 구도 단위 조각. 모든 화면은 아래 세 구도 중 하나로만 짠다.
//  A. 포스터 캐러셀(21:122): PosterCard + PosterRow
//  B. 꽉 찬 히어로(21:7): HeroScreen + HeroHeader (+ 오른쪽 유리 카드 하나까지)
//  C. 장면(21:52, 21:88): SceneScreen + ChapterProgress
// =====================================================================

/** 선택되지 않은 것의 투명도(캐러셀 양옆 카드, 직전 대사) */
const val DIM_ALPHA = .4f

/**
 * A. 세로 포스터 카드. 가운데(focused)는 330×500, 양옆은 310×470이고 이미지가 보이도록 투명도만 낮춘다.
 * 카드 아래쪽 그라데이션 위에 흰 제목·메타·두 줄 소개를 가운데 정렬로 둔다.
 */
@Composable
fun PosterCard(
    bot: StoryBot,
    modifier: Modifier = Modifier,
    title: String = bot.title,
    meta: List<String> = emptyList(),
    body: String = bot.description,
    focused: Boolean = true,
    image: Int? = bot.cover,
    badge: String? = null,
) {
    val textAlpha = if (focused) 1f else .6f
    Box(modifier.size(if (focused) 330.dp else 310.dp, if (focused) 500.dp else 470.dp).alpha(if (focused) 1f else .55f)) {
        Hero(bot, Modifier.fillMaxSize(), image = image, scrim = Scrim.BOTTOM)
        badge?.let {
            Text(
                it, Modifier.padding(16.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .85f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = Momo.Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            )
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title, color = Color.White.copy(alpha = textAlpha), fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, maxLines = 2, style = KoBreak,
            )
            if (meta.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                MetaLine(meta, Color.White.copy(alpha = .8f * textAlpha))
            }
            Spacer(Modifier.height(18.dp))
            Text(
                body, Modifier.fillMaxWidth(), color = Color.White.copy(alpha = textAlpha), fontSize = 15.sp, lineHeight = 23.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * A. 포스터 캐러셀. 화면 가운데에 가장 가까운 카드가 강조 카드이고, 스냅으로 멈춘다.
 * 가운데 카드를 누르면 onOpen, 양옆 카드를 누르면 그 카드를 가운데로 데려온다.
 */
@Composable
fun <T> PosterRow(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    onOpen: (T) -> Unit,
    card: @Composable (item: T, focused: Boolean) -> Unit,
) {
    val list = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val scope = rememberCoroutineScope()
    val center by remember {
        derivedStateOf {
            val info = list.layoutInfo
            val mid = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - mid) }?.index ?: 0
        }
    }
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        LazyRow(
            state = list,
            flingBehavior = rememberSnapFlingBehavior(list),
            contentPadding = PaddingValues(horizontal = (maxWidth - 330.dp) / 2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(items, key = { _, it -> key(it) }) { i, item ->
                Box(Modifier.clickable { if (i == center) onOpen(item) else scope.launch { list.animateScrollToItem(i) } }) {
                    card(item, i == center)
                }
            }
        }
    }
}

/**
 * B. 꽉 찬 히어로 화면. 이미지가 앱 영역을 채우고(좌우·아래 Gutter), 왼쪽 55%에 텍스트 스택을 세로 가운데로 쌓는다.
 * side에는 오른쪽 유리 카드(또는 포스터 카드) 하나만 둔다.
 */
@Composable
fun HeroScreen(
    bot: StoryBot,
    image: Int? = bot.scenes.firstOrNull() ?: bot.cover,
    side: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) = Hero(
    bot, Modifier.fillMaxSize().padding(start = Gutter, end = Gutter, bottom = Gutter).clip(RoundedCornerShape(4.dp)), image = image,
) {
    Column(
        Modifier.fillMaxHeight().fillMaxWidth(.55f).padding(start = 48.dp, end = 16.dp),
        verticalArrangement = Arrangement.Center,
        content = content,
    )
    side?.let { Box(Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)) { it() } }
}

/** B. 히어로 왼쪽 텍스트 스택: 머리말 → 큰 굵은 제목 → 부제 → 메타 줄 → 두 줄 소개 (21:7 순서) */
@Composable
fun HeroHeader(kicker: String, title: String, subtitle: String? = null, meta: List<String> = emptyList(), body: String? = null) {
    val white = Color.White
    Text(kicker, color = white, fontSize = 22.sp)
    Spacer(Modifier.height(10.dp))
    Text(title, color = white, fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, maxLines = 2, style = KoBreak)
    subtitle?.let {
        Spacer(Modifier.height(4.dp))
        Text(it, color = white, fontSize = 21.sp, lineHeight = 28.sp, maxLines = 2, style = KoBreak)
    }
    if (meta.isNotEmpty()) {
        Spacer(Modifier.height(20.dp))
        MetaLine(meta, white.copy(alpha = .7f))
    }
    body?.let {
        Spacer(Modifier.height(20.dp))
        Text(it, color = white, fontSize = 18.sp, lineHeight = 28.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, style = KoBreak)
    }
}

/**
 * C. 장면 화면. dimmed(주행 중)이면 배경을 강하게 흐리고 밝게 덮으며 글자는 검정,
 * 아니면(정차 중) 장면 그림을 선명하게 두고 글자는 흰색이다. 애니메이션은 쓰지 않는다.
 * content는 왼쪽에서 약 22% 지점부터 쌓이고, bottomCenter에는 유리 카드를 둔다.
 */
@Composable
fun SceneScreen(
    bot: StoryBot,
    image: Int?,
    dimmed: Boolean,
    top: @Composable (ink: Color) -> Unit,
    bottomStart: @Composable (ink: Color) -> Unit = {},
    bottomCenter: @Composable ColumnScope.(ink: Color) -> Unit = {},
    content: @Composable ColumnScope.(ink: Color) -> Unit,
) {
    val ink = if (dimmed) Momo.Ink else Color.White
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (dimmed) {
            Cover(bot, Modifier.fillMaxSize().blur(36.dp, BlurredEdgeTreatment.Rectangle), image)
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = .62f)))
        } else {
            Cover(bot, Modifier.fillMaxSize(), image)
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Black.copy(alpha = .45f), .5f to Color.Black.copy(alpha = .2f), 1f to Color.Black.copy(alpha = .5f)),
                ),
            )
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 20.dp)) { top(ink) }
        Column(Modifier.fillMaxWidth().padding(start = maxWidth * .216f, end = 80.dp, top = 100.dp)) { content(ink) }
        Box(Modifier.align(Alignment.BottomStart).padding(28.dp)) { bottomStart(ink) }
        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            bottomCenter(ink)
        }
    }
}

/** C. 상단 장 진행: 장마다 4dp 막대 한 칸과 그 아래 작은 장 제목. 지난 장과 지금 장은 채운다. */
@Composable
fun ChapterProgress(titles: List<String>, current: Int, ink: Color, modifier: Modifier = Modifier) =
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        titles.forEachIndexed { i, title ->
            Column(Modifier.weight(1f)) {
                ProgressBar(if (i <= current) 1f else 0f, ink, Modifier.fillMaxWidth())
                Spacer(Modifier.height(7.dp))
                Text(title, color = ink.copy(alpha = if (i <= current) 1f else .5f), fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
