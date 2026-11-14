package app.momo.ui.overlays

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import app.momo.core.BotStatus
import app.momo.core.Screen
import app.momo.core.Turn
import app.momo.engine.CreateState
import app.momo.engine.ImageState
import app.momo.ui.Momo
import app.momo.voice.VoiceStatus

/** 시연 백업: 웹 데모 QA처럼 콩이 예시 답변을 차례로 넣는다 */
private val DEMO_ANSWERS = listOf(
    "우리 강아지 콩이! 하얗고 겁 많은데, 멍멍하면서 존댓말 써.",
    "우리 동네 놀이터가 탐정 사무소야.",
    "밝은 아이 목소리로!",
)

/**
 * S1-1 말로 만들기. 왼쪽은 AI 질문과 내 대답, 오른쪽은 만들어지는 봇 미리보기다.
 * 화면은 Graph.creator.state만 보고 그린다. 주행이 시작되면 MomoApp이 숨기고(일시정지) 내용은 creator에 남는다.
 */
@Composable
fun CreateOverlay(app: AppState, car: CarState) {
    val cs by Graph.creator.state.collectAsStateWithLifecycle()

    // 마이크(STT)·adb 주입·시연 버튼·입력칸이 모두 voice.heard로 들어오고, 여기서 칸 채우기로 넘긴다
    LaunchedEffect(Unit) {
        Graph.voice.heard.collect { Graph.voice.stopListening(); Graph.creator.answer(it) }
    }
    // 확정된 봇은 마이페이지에 자동 저장한다. 다시 저장할 때 이미 바뀐 공개 상태(검수 중 등)는 그대로 둔다.
    LaunchedEffect(cs.bot) {
        val bot = cs.bot ?: return@LaunchedEffect
        val old = AppStore.state.value.bots.firstOrNull { it.id == bot.id }
        AppStore.addMyBot(bot.copy(status = old?.status ?: BotStatus.PRIVATE))
    }

    fun exit() { Graph.creator.reset(); AppStore.close() }

    Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) {}) {
        // 표지를 흐리게 깔고(완성 전에는 지금 고른 봇, 완성 후에는 만든 봇) 그 위에 어두운 스크림. 뒤 화면 글자가 비치지 않게 한다.
        val backdrop = cs.bot?.cover?.takeIf { cs.image == ImageState.DONE } ?: app.bot.cover
        if (backdrop != null) Image(painterResource(backdrop), null, Modifier.matchParentSize().blur(40.dp), contentScale = ContentScale.Crop)
        else Box(Modifier.matchParentSize().background(Momo.Bg))
        Box(Modifier.matchParentSize().background(Momo.Scrim))
        GlassCard(Modifier.padding(horizontal = 24.dp, vertical = 20.dp).fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                Talk(cs, Modifier.weight(1f).fillMaxHeight(), ::exit)
                Preview(cs, Modifier.width(340.dp).fillMaxHeight(), ::exit)
            }
        }
    }
}

@Composable
private fun Talk(cs: CreateState, modifier: Modifier, exit: () -> Unit) = Column(modifier) {
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text("말로 새 이야기 봇 만들기", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Meta(listOf("빈 칸만 다시 물어봐요", "출발하면 일시정지"), size = 14.sp)
        }
        RoundButton("✕", onClick = exit)
    }
    Spacer(Modifier.height(18.dp))
    // 네 칸 진행: Figma 21:52 장 진행 막대처럼 칸마다 얇은 막대 하나
    val slots = listOf("캐릭터" to cs.draft.character, "말투" to cs.draft.tone, "세계관" to cs.draft.world, "목소리" to cs.draft.voice)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.forEach { (label, v) ->
            Column(Modifier.weight(1f)) {
                ProgressBar(if (v != null) 1f else 0f, Modifier.fillMaxWidth())
                Text(label, Modifier.padding(top = 6.dp).alpha(if (v != null) 1f else DIM), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Spacer(Modifier.height(18.dp))

    val list = rememberLazyListState()
    val tail = if (cs.busy) "생각 중…" else cs.question
    LaunchedEffect(cs.dialog.size, tail) { list.animateScrollToItem(cs.dialog.size) }
    LazyColumn(Modifier.weight(1f).fillMaxWidth(), list, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 지난 대화는 흐리게, 마지막 대답과 지금 질문은 선명하게
        itemsIndexed(cs.dialog) { i, t -> Line(t, dim = i < cs.dialog.size - 1) }
        item { Line(Turn("AI", tail), thinking = cs.busy) }
    }

    Spacer(Modifier.height(12.dp))
    val demo = DEMO_ANSWERS.getOrNull(cs.dialog.count { it.speaker == "나" })
    if (demo != null && cs.bot == null && !cs.busy) Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        PillButton("시연: 답변 넣기", light = true, height = 36.dp) { Graph.voice.inject(demo) }
        Spacer(Modifier.width(10.dp))
        Text("“$demo”", fontSize = 13.sp, color = Momo.Sub, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    InputBar(enabled = cs.bot == null && !cs.busy)
}

/** 대화 한 줄. AI는 Figma 21:52처럼 작은 화자 이름 + 굵은 글, 나는 오른쪽 검정 말풍선 */
@Composable
private fun Line(t: Turn, dim: Boolean = false, thinking: Boolean = false) {
    val me = t.speaker == "나"
    Box(Modifier.fillMaxWidth().alpha(if (dim) DIM else 1f), contentAlignment = if (me) Alignment.CenterEnd else Alignment.CenterStart) {
        if (me) Text(
            t.text, Modifier.fillMaxWidth(0.8f).wrapContentWidth(Alignment.End)
                .background(Momo.Ink, RoundedCornerShape(20.dp)).padding(horizontal = 18.dp, vertical = 12.dp),
            color = Momo.OnAccent, fontSize = 16.sp,
        )
        else Column(Modifier.fillMaxWidth(0.85f)) {
            Text("MOMO", fontSize = 12.sp, color = Momo.Sub, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (thinking) { CircularProgressIndicator(Modifier.size(18.dp), color = Momo.Sub, strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)) }
                Text(t.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (thinking) Momo.Sub else Momo.Ink)
            }
        }
    }
}

/** 정차 중이라 허용되는 텍스트 입력과 마이크 */
@Composable
private fun InputBar(enabled: Boolean) {
    val status by Graph.voice.status.collectAsStateWithLifecycle()
    val partial by Graph.voice.partial.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    fun send() { if (text.isNotBlank()) { Graph.voice.inject(text.trim()); text = "" } }
    val listening = status == VoiceStatus.LISTENING

    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(50)).background(Momo.Ink.copy(alpha = 0.06f)).padding(start = 22.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text(
                if (listening) "듣고 있어요 · “${partial.ifEmpty { "…" }}”" else "마이크를 누르고 말하거나 여기에 써 주세요",
                fontSize = 15.sp, color = if (listening) Momo.Ink else Momo.Sub, maxLines = 1,
            )
            BasicTextField(
                text, { text = it }, Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = Momo.Ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions { send() },
            )
        }
        if (text.isNotBlank()) Text("보내기", Modifier.clickable(onClick = ::send).padding(horizontal = 14.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Box(
            Modifier.size(44.dp).alpha(if (enabled) 1f else DIM).clip(CircleShape).background(Momo.Ink)
                .clickable(enabled = enabled) { if (listening) Graph.voice.stopListening() else Graph.voice.listen() },
            contentAlignment = Alignment.Center,
        ) { if (listening) Text("■", color = Momo.OnAccent, fontSize = 14.sp) else MicIcon() }
    }
}

/** 마이크 모양(흰 선). 아이콘 라이브러리 없이 그린다. */
@Composable
private fun MicIcon() = Canvas(Modifier.size(20.dp)) {
    val w = size.width
    val s = Stroke(width = w * 0.1f)
    drawRoundRect(Color.White, Offset(w * 0.35f, 0f), Size(w * 0.3f, w * 0.6f), CornerRadius(w * 0.15f))
    drawArc(Color.White, 0f, 180f, false, Offset(w * 0.2f, w * 0.25f), Size(w * 0.6f, w * 0.55f), style = s)
    drawLine(Color.White, Offset(w / 2, w * 0.8f), Offset(w / 2, w), strokeWidth = w * 0.1f)
}

@Composable
private fun Preview(cs: CreateState, modifier: Modifier, exit: () -> Unit) = Column(modifier) {
    val status by Graph.voice.status.collectAsStateWithLifecycle()
    val bot = cs.bot
    val imageMod = Modifier.fillMaxWidth().height(220.dp)
    if (cs.image == ImageState.DONE) CoverCard(bot, imageMod) {
        CardTitle(bot?.title ?: "새 이야기 봇")
        Meta(listOf("말로 만든 봇", "자동 저장됨"), color = OnImageSub, size = 12.sp)
    } else Box(imageMod.clip(Round), contentAlignment = Alignment.Center) {
        Cover(null, Modifier.matchParentSize())
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (cs.image == ImageState.GENERATING) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                Spacer(Modifier.height(14.dp))
                Text("이미지 생성 중…", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("말한 특징으로 모습을 그리고 있어요", color = OnImageSub, fontSize = 13.sp)
            } else {
                Text("특징을 다 말하면", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("이미지가 만들어져요", color = OnImageSub, fontSize = 13.sp)
            }
        }
    }
    Spacer(Modifier.height(14.dp))

    // 지금 듣고 있는 칸은 첫 번째 빈 칸으로 보여 준다
    val listeningSlot = if (status == VoiceStatus.LISTENING) cs.draft.missing.firstOrNull() else null
    listOf("캐릭터" to cs.draft.character, "말투" to cs.draft.tone, "세계관" to cs.draft.world, "목소리" to cs.draft.voice).forEach { (label, value) ->
        val on = value != null || label == listeningSlot
        Column(Modifier.padding(vertical = 5.dp).alpha(if (on) 1f else DIM)) {
            Text(label, fontSize = 12.sp, color = Momo.Sub)
            Text(value ?: if (label == listeningSlot) "듣는 중…" else "빈 칸", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    Spacer(Modifier.height(6.dp))
    Meta(
        listOf("역할 " + (bot?.roles?.joinToString(", ") ?: "자동"), "시작 " + (bot?.minsLabel ?: "경로 시간별 자동")),
        size = 12.sp, modifier = Modifier.alpha(if (bot != null) 1f else DIM),
    )

    Spacer(Modifier.weight(1f))
    if (bot == null) {
        val generating = cs.image == ImageState.GENERATING
        PillButton(
            if (generating) "만드는 중…" else "이대로 만들기 (이미지 생성)", Modifier.fillMaxWidth(), icon = "✓",
            enabled = cs.draft.complete && !cs.busy && !generating,
        ) { Graph.creator.confirm() }
        if (!cs.draft.complete) Text("네 칸이 다 차면 만들 수 있어요", Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally), fontSize = 12.sp, color = Momo.Sub)
    } else {
        Text(
            "↻ 이미지 다시 만들기", Modifier.align(Alignment.End).alpha(if (cs.image == ImageState.GENERATING) DIM else 1f)
                .clickable(enabled = cs.image != ImageState.GENERATING) { Graph.creator.regenerateImage() }.padding(vertical = 8.dp),
            fontSize = 14.sp, fontWeight = FontWeight.Bold,
        )
        PillButton("이 봇으로 이야기 시작", Modifier.fillMaxWidth(), icon = "▶") { val id = bot.id; exit(); AppStore.selectBot(id, Screen.PLAYERS) }
        Spacer(Modifier.height(8.dp))
        PillButton("마켓에 올리기", Modifier.fillMaxWidth(), light = true) { val id = bot.id; exit(); goUpload(id) }
        Text("만든 봇은 마이페이지에 자동 저장돼요", Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally), fontSize = 12.sp, color = Momo.Sub)
    }
}
