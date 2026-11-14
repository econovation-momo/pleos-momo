package app.momo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.StoryBot
import app.momo.ui.Momo

/** 이야기 길이(분): 내비 남은 시간이 우선이고, 없으면 '목적지 없이 시작'에서 고른 값, 그것도 없으면 30분. */
fun rideMin(app: AppState, car: CarState): Int = car.remainSec?.let { it / 60 } ?: app.rideMin ?: 30

/** 목표 장면 수. 엔진과 같은 기준(2분에 한 장면)으로 미리 보여 준다. */
fun scenesFor(min: Int) = (min / 2).coerceAtLeast(3)

fun destLabel(app: AppState, car: CarState) =
    car.destination?.let { "${it}까지 ${rideMin(app, car)}분" } ?: "목적지 없이 ${rideMin(app, car)}분 이야기"

/** Figma 카드 메타 줄: 2026 | 키즈 | 45분 | 어드벤처 */
fun metaLine(bot: StoryBot, min: Int) = "2026  |  ${bot.rating.label}  |  ${bot.startFor(min).minutes}분  |  ${bot.genre}"

/** 한국어 제목·대사를 어절 단위로 끊고 줄 길이를 고르게 맞춘다(API 33 이상에서 동작). */
val KoBreak = TextStyle(
    lineBreak = LineBreak(LineBreak.Strategy.Balanced, LineBreak.Strictness.Strict, LineBreak.WordBreak.Phrase),
)

/** 대표 이미지. 그림이 없는 봇은 어두운 바탕에 제목 첫 글자를 둔다. */
@Composable
fun Cover(bot: StoryBot, modifier: Modifier = Modifier, image: Int? = bot.cover) {
    if (image != null) Image(painterResource(image), bot.title, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF2B2F38), Color(0xFF111111)))), Alignment.Center) {
        Text(bot.title.take(1), color = Color.White.copy(alpha = .5f), fontSize = 64.sp, fontWeight = FontWeight.Black)
    }
}

/** 둥근 알약 버튼. dark=true면 검정 바탕, light=true면 흰 바탕(이미지 위). */
@Composable
fun PillButton(
    text: String,
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    fontSize: TextUnit = 16.sp,
    height: Dp = 48.dp,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier.height(height).clip(RoundedCornerShape(50)).background(if (dark) Momo.Ink else Color.White)
            .border(if (dark) 0.dp else 1.dp, Momo.Line, RoundedCornerShape(50))
            .clickable(onClick = onClick).padding(horizontal = 21.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        leading?.let { it(); Spacer(Modifier.width(9.dp)) }
        Text(text, color = if (dark) Color.White else Momo.Ink, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

/** 작은 칩. on이면 검정, 아니면 회색. */
@Composable
fun Chip(text: String, on: Boolean = false, modifier: Modifier = Modifier, fontSize: TextUnit = 15.sp, onClick: (() -> Unit)? = null) {
    Text(
        text,
        modifier.background(if (on) Momo.Ink else Color.White, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        color = if (on) Color.White else Momo.Ink, fontSize = fontSize, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
    )
}

/** 화면에 쓰는 차량 API 표기(심사위원용). */
@Composable
fun ApiTag(text: String) = Text(
    text,
    Modifier.background(Color(0xFFE8F4F1), RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 4.dp),
    color = Color(0xFF1E6B5E), fontSize = 15.sp, fontFamily = FontFamily.Monospace,
)

/** 흰 카드. Figma 1차 시안의 둥근 회색 카드. */
@Composable
fun Card(modifier: Modifier = Modifier, color: Color = Momo.Surface, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.background(color, RoundedCornerShape(16.dp)).padding(16.dp), content = content)

/** 큰 숫자 카드: 제목 / 숫자+단위 / 작은 설명 칩 */
@Composable
fun StatCard(title: String, value: String, unit: String, note: String?, modifier: Modifier = Modifier) = Card(modifier) {
    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.weight(1f))
    Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.Bottom) {
        Text(value, fontSize = 43.sp, fontWeight = FontWeight.Light, lineHeight = 43.sp)
        Text(" $unit", fontSize = 15.sp, color = Momo.Sub, modifier = Modifier.padding(bottom = 5.dp))
    }
    Spacer(Modifier.weight(1f))
    note?.let { Chip(it, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 15.sp) }
}

/** 마이크 아이콘. 아이콘 라이브러리를 들이지 않으려고 직접 그린다. */
@Composable
fun MicIcon(color: Color = Color.White, size: Dp = 19.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    drawRoundRect(color, Offset(w * .35f, 0f), Size(w * .3f, h * .6f), CornerRadius(w * .15f))
    drawArc(color, 0f, 180f, false, Offset(w * .2f, h * .25f), Size(w * .6f, h * .5f), style = Stroke(w * .08f))
    drawLine(color, Offset(w / 2, h * .75f), Offset(w / 2, h * .95f), w * .08f)
}

/** 원 안에 이름 첫 글자. 좌석 도식과 목록에서 같이 쓴다. */
@Composable
fun Avatar(name: String?, active: Boolean, size: Dp = 43.dp, modifier: Modifier = Modifier) = Box(
    modifier.size(size).background(if (active) Momo.Ink else Color.White, CircleShape)
        .border(if (name == null) 2.dp else 3.dp, if (active) Color.White else Momo.Line, CircleShape),
    Alignment.Center,
) {
    if (name != null) Text(
        (if (name in setOf("엄마", "아빠")) name.take(1) else name.takeLast(1)), color = if (active) Color.White else Momo.Ink, fontSize = (size.value * .36f).sp,
        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
    )
}

/** 검정 원 안의 재생 삼각형(Figma '이어서 시청' 버튼 아이콘) */
@Composable
fun PlayIcon(size: Dp = 29.dp) = Box(Modifier.size(size).background(Momo.Ink, CircleShape), Alignment.Center) {
    Canvas(Modifier.size(size * .36f)) {
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(this@Canvas.size.width * .15f, 0f)
            lineTo(this@Canvas.size.width, this@Canvas.size.height / 2)
            lineTo(this@Canvas.size.width * .15f, this@Canvas.size.height)
            close()
        }
        drawPath(p, Color.White)
    }
}

fun minLabel(m: Int) = if (m >= 60 && m % 60 == 0) "${m / 60}시간" else "${m}분"
