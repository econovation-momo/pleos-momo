package app.momo.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.momo.Graph
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Screen
import app.momo.engine.CreateState
import app.momo.engine.ImageState
import app.momo.ui.Momo
import app.momo.ui.components.*
import app.momo.voice.VoiceStatus
import kotlinx.coroutines.delay

/** 시연 백업: 웹 데모 QA처럼 콩이 예시 답변을 차례로 넣는다 */
private val DEMO_ANSWERS = listOf(
    "우리 강아지 콩이! 하얗고 겁 많은데, 멍멍하면서 존댓말 써.",
    "우리 동네 놀이터가 탐정 사무소야.",
    "밝은 아이 목소리로!",
)

/**
 * S1-1 말로 만들기: Figma 21:52·21:88 장면 구도. 상단 네 칸 진행 막대, 왼쪽 22% 지점에 AI 질문을 대사처럼 크게,
 * 하단 가운데 '방금 들은 말' 유리 카드, 오른쪽에 만들어지는 봇 포스터 카드 한 장, 맨 아래 입력과 확정 알약.
 * 화면은 Graph.creator.state만 보고 그린다. 마이페이지 저장은 BotCreator가 한다.
 * 주행이 시작되면 MomoApp이 숨기고(일시정지) 내용은 creator에 남는다.
 */
@Composable
fun CreateOverlay(app: AppState, car: CarState) {
    val cs by Graph.creator.state.collectAsStateWithLifecycle()

    // 마이크(STT)·adb 주입·시연 버튼·입력칸이 모두 voice.heard로 들어오고, 여기서 칸 채우기로 넘긴다
    LaunchedEffect(Unit) {
        Graph.voice.heard.collect { Graph.voice.stopListening(); Graph.creator.answer(it) }
    }
    fun exit() { Graph.creator.reset(); AppStore.close() }
    val bot = cs.bot

    BoxWithConstraints(
        Modifier.fillMaxSize().background(Color.Black).clickable(remember { MutableInteractionSource() }, indication = null) {},
    ) {
        // 배경: 봇이 생기면 그 표지를 흐리게, 그 전에는 어두운 그라데이션
        if (bot != null) Cover(bot, Modifier.fillMaxSize().blur(28.dp, BlurredEdgeTreatment.Rectangle))
        else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF3A3F4B), Color(0xFF111111)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = .45f), .5f to Color.Black.copy(alpha = .25f), 1f to Color.Black.copy(alpha = .6f))))

        SlotBar(cs, Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 20.dp), ::exit)

        // 왼쪽 22% 지점: 직전 문답은 흐리게, 지금 질문은 크게
        val prev = cs.dialog.dropLast(1).lastOrNull { it.speaker == "AI" }?.text?.takeIf { bot == null }
        Column(Modifier.padding(start = maxWidth * .216f, end = 420.dp, top = 120.dp)) {
            Text(prev ?: "", color = Color.White.copy(alpha = .3f), fontSize = 21.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(36.dp))
            Text("MOMO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(
                if (cs.busy) "생각 중…" else if (bot != null) "${bot.character.name}${iGa(bot.character.name)} 완성됐어요! 오른쪽 카드를 확인해 보세요." else cs.question,
                color = Color.White.copy(alpha = if (cs.busy) .6f else 1f), fontSize = 34.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold,
                maxLines = 3, overflow = TextOverflow.Ellipsis, style = KoBreak,
            )
        }

        Preview(cs, Modifier.align(Alignment.CenterEnd).padding(end = 40.dp, top = 30.dp))

        // 하단: '방금 들은 말' 유리 카드, 그 아래 입력과 확정 알약
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            cs.dialog.lastOrNull { it.speaker == "나" }?.let { Heard(it.text) }
            Spacer(Modifier.height(14.dp))
            Controls(cs, ::exit)
        }
    }
}

/** 상단 네 칸 진행 막대(21:52 장 진행 막대와 같은 모양)와 닫기 */
@Composable
private fun SlotBar(cs: CreateState, modifier: Modifier, exit: () -> Unit) = Row(modifier, verticalAlignment = Alignment.Top) {
    val slots = listOf("캐릭터" to cs.draft.character, "말투" to cs.draft.tone, "세계관" to cs.draft.world, "목소리" to cs.draft.voice)
    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        slots.forEach { (label, v) ->
            Column(Modifier.weight(1f)) {
                ProgressBar(if (v != null) 1f else 0f, Color.White, Modifier.fillMaxWidth())
                Spacer(Modifier.height(7.dp))
                Text(
                    v?.let { "$label · $it" } ?: label, color = Color.White.copy(alpha = if (v != null) 1f else .5f),
                    fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    Spacer(Modifier.width(24.dp))
    RoundButton("✕", onImage = true, onClick = exit)
}

/** 오른쪽 포스터 카드: 없음은 그라데이션, 생성 중은 진행 막대, 완료는 표지. 아래 메타에 네 칸 채움 상태. */
@Composable
private fun Preview(cs: CreateState, modifier: Modifier) = Column(modifier.width(260.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    val bot = cs.bot
    val name = bot?.title ?: cs.draft.name ?: "새 이야기 봇"
    val shown = if (cs.image == ImageState.DONE) bot else null
    PosterCard(
        shown, name, emptyList(), cs.draft.world?.let { "${it}에서 펼쳐지는 이야기" } ?: "말한 특징으로 표지를 만들어요",
        Modifier.size(260.dp, 390.dp), titleSize = 24,
        top = { ImageTag(if (cs.image == ImageState.DONE) "마이페이지에 저장됨" else "만드는 중", dark = cs.image == ImageState.DONE) },
        extra = {
            if (cs.image == ImageState.GENERATING) {
                // 이미지 생성 API 응답을 기다리는 동안 채워지는 막대(BotCreator는 약 2.5초)
                var f by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) { while (f < .95f) { delay(50); f += .02f } }
                Text("그림 그리는 중…", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                ProgressBar(f, Color.White, Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
            }
        },
    )
    Spacer(Modifier.height(12.dp))
    // 네 칸 채움 상태: 채워진 것만 선명하게
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("캐릭터" to cs.draft.character, "말투" to cs.draft.tone, "세계관" to cs.draft.world, "목소리" to cs.draft.voice).forEach { (l, v) ->
            Text(l, color = Color.White.copy(alpha = if (v != null) 1f else .4f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
    bot?.let {
        Spacer(Modifier.height(4.dp))
        MetaLine(listOf(it.roles.joinToString(", "), it.minutes.joinToString("·") { m -> minLabel(m) }), OnImageSub, fontSize = 13.sp)
    }
}

/** 하단 가운데 '방금 들은 말' 유리 카드(21:88 정차 중 모양: 장면이 비치도록 옅게) */
@Composable
private fun Heard(text: String) = Column(
    Modifier.width(600.dp)
        .background(Color.White.copy(alpha = .22f), RoundedCornerShape(20.dp))
        .border(1.dp, Color.White.copy(alpha = .9f), RoundedCornerShape(20.dp))
        .padding(horizontal = 24.dp, vertical = 14.dp),
) {
    Text("방금 들은 말 · 나", color = Color.White.copy(alpha = .7f), fontSize = 15.sp)
    Text("“$text”", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** 맨 아래 알약 줄: 마이크·입력칸·시연 답변(만드는 동안), 확정 또는 완성 후 버튼 */
@Composable
private fun Controls(cs: CreateState, exit: () -> Unit) {
    val status by Graph.voice.status.collectAsStateWithLifecycle()
    val partial by Graph.voice.partial.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val listening = status == VoiceStatus.LISTENING
    val bot = cs.bot
    // LLM을 기다리는 동안 들어온 답도 BotCreator가 순서대로 처리하므로 busy여도 입력은 열어 둔다('생각 중' 표시만 한다)
    val inputOn = bot == null && cs.image != ImageState.GENERATING
    // 처리 대기 중인 답은 아직 dialog에 없으니, 시연 답변은 보낸 개수로 다음 차례를 센다
    var demoSent by remember { mutableIntStateOf(0) }
    val demoIndex = maxOf(cs.dialog.count { it.speaker == "나" }, demoSent)
    fun send() { if (text.isNotBlank()) { Graph.voice.inject(text.trim()); text = "" } }

    Row(Modifier.padding(start = 28.dp, end = 110.dp), verticalAlignment = Alignment.CenterVertically) {
        if (bot == null) {
            // 마이크: 듣는 중이면 흰 원으로 채운다
            Box(
                Modifier.size(56.dp).alpha(if (inputOn) 1f else DIM)
                    .background(if (listening) Color.White else Color.White.copy(alpha = .18f), CircleShape)
                    .clickable(enabled = inputOn) { if (listening) Graph.voice.stopListening() else Graph.voice.listen() },
                Alignment.Center,
            ) { MicGlyph(if (listening) Momo.Ink else Color.White, 24.dp) }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.width(340.dp).height(52.dp).background(Color.White.copy(alpha = .18f), RoundedCornerShape(50)).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (text.isEmpty()) Text(
                    if (listening) "듣고 있어요 · “${partial.ifEmpty { "…" }}”" else "말하거나 여기에 써 주세요",
                    color = Color.White.copy(alpha = .7f), fontSize = 16.sp, maxLines = 1,
                )
                BasicTextField(
                    text, { text = it }, Modifier.fillMaxWidth(), enabled = inputOn, singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions { send() },
                )
            }
            if (text.isNotBlank()) { Spacer(Modifier.width(8.dp)); GhostButton("보내기", fontSize = 16.sp, onClick = ::send) }
            val demo = DEMO_ANSWERS.getOrNull(demoIndex)
            if (demo != null && inputOn && text.isBlank()) {
                Spacer(Modifier.width(8.dp))
                GhostButton("시연: 답변 넣기", fontSize = 16.sp) { demoSent = demoIndex + 1; Graph.voice.inject(demo) }
            }
            Spacer(Modifier.weight(1f))
            val generating = cs.image == ImageState.GENERATING
            PillButton(
                if (generating) "만드는 중…" else "이대로 만들기", Modifier.alpha(if (cs.draft.complete && !cs.busy && !generating) 1f else DIM),
                height = 56.dp, fontSize = 18.sp, icon = { Text("✓", color = it, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            ) { if (cs.draft.complete && !cs.busy && !generating) Graph.creator.confirm() }
        } else {
            Text("만든 봇은 마이페이지에 자동 저장돼요", color = OnImageSub, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            GhostButton("이미지 다시 만들기", Modifier.alpha(if (cs.image == ImageState.GENERATING) DIM else 1f), fontSize = 16.sp) {
                if (cs.image != ImageState.GENERATING) Graph.creator.regenerateImage()
            }
            Spacer(Modifier.width(8.dp))
            GhostButton("마켓에 올리기", fontSize = 16.sp) { val id = bot.id; exit(); goUpload(id) }
            Spacer(Modifier.width(8.dp))
            PillButton("이 봇으로 이야기 시작", height = 56.dp, fontSize = 18.sp) { val id = bot.id; exit(); AppStore.selectBot(id, Screen.PLAYERS) }
        }
    }
}
