package app.momo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Figma 시안(흰 바탕, 검정 강조) 기준 색. 디자이너 시안이 바뀌면 여기만 고친다. */
object Momo {
    val Bg = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF2F3F5)
    val Ink = Color(0xFF111111)
    val Sub = Color(0xFF6B7078)
    val Line = Color(0xFFE3E5E8)
    val Accent = Color(0xFF111111)     // 주요 버튼
    val OnAccent = Color(0xFFFFFFFF)
    val Point = Color(0xFFFFB547)      // 키즈·강조 포인트
    val Scrim = Color(0x99000000)
}

@Composable
fun MomoTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = lightColorScheme(
        primary = Momo.Accent, onPrimary = Momo.OnAccent, background = Momo.Bg, surface = Momo.Bg,
        onBackground = Momo.Ink, onSurface = Momo.Ink, surfaceVariant = Momo.Surface, outline = Momo.Line,
    ),
    content = content,
)
