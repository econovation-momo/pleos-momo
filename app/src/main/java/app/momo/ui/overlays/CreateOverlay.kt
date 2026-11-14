package app.momo.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    Box(
        Modifier.fillMaxSize().background(Momo.Scrim).clickable(remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.padding(horizontal = 40.dp, vertical = 24.dp).fillMaxSize()
                .clip(RoundedCornerShape(24.dp)).background(Momo.Bg).padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Talk(cs, Modifier.weight(1f).fillMaxHeight(), ::exit)
            Preview(cs, Modifier.width(360.dp).fillMaxHeight(), ::exit)
        }
    }
}

@Composable
private fun Talk(cs: CreateState, modifier: Modifier, exit: () -> Unit) = Column(modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBox("✕", onClick = exit)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("말로 새 이야기 봇 만들기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("빈 칸만 다시 물어봐요 · 출발하면 일시정지", fontSize = 13.sp, color = Momo.Sub)
        }
        Badge("${4 - cs.draft.missing.size} / 4 칸", dark = true)
    }
    Spacer(Modifier.height(16.dp))

    val list = rememberLazyListState()
    val tail = if (cs.busy) "생각 중…" else cs.question
    LaunchedEffect(cs.dialog.size, tail) { list.animateScrollToItem(cs.dialog.size) }
    LazyColumn(Modifier.weight(1f).fillMaxWidth(), list, verticalArrangement = Arrangement.spacedBy(11.dp)) {
        items(cs.dialog) { Bubble(it) }
        item { Bubble(Turn("AI", tail), thinking = cs.busy) }
    }

    Spacer(Modifier.height(12.dp))
    val demo = DEMO_ANSWERS.getOrNull(cs.dialog.count { it.speaker == "나" })
    if (demo != null && cs.bot == null && !cs.busy) {
        Text(
            "시연: 답변 넣기 · “$demo”",
            Modifier.padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Momo.Line, RoundedCornerShape(12.dp))
                .clickable { Graph.voice.inject(demo) }.padding(horizontal = 13.dp, vertical = 8.dp),
            fontSize = 13.sp, color = Momo.Sub, maxLines = 1,
        )
    }
    InputBar(enabled = cs.bot == null && !cs.busy)
}

@Composable
private fun Bubble(t: Turn, thinking: Boolean = false) {
    val me = t.speaker == "나"
    Box(Modifier.fillMaxWidth(), contentAlignment = if (me) Alignment.CenterEnd else Alignment.CenterStart) {
        Row(
            Modifier.fillMaxWidth(0.85f).wrapContentWidth(if (me) Alignment.End else Alignment.Start)
                .background(if (me) Momo.Ink else Momo.Surface, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (thinking) { CircularProgressIndicator(Modifier.size(15.dp), color = Momo.Sub, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
            Text(t.text, color = if (me) Momo.OnAccent else if (thinking) Momo.Sub else Momo.Ink, fontSize = 15.sp)
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
        Modifier.fillMaxWidth().background(Momo.Surface, RoundedCornerShape(24.dp)).padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text(
                if (listening) "듣고 있어요 · “${partial.ifEmpty { "…" }}”" else "마이크를 누르고 말하거나 여기에 써 주세요",
                fontSize = 14.sp, color = Momo.Sub, maxLines = 1,
            )
            BasicTextField(
                text, { text = it }, Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = Momo.Ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions { send() },
            )
        }
        if (text.isNotBlank()) Text("보내기", Modifier.clickable(onClick = ::send).padding(horizontal = 12.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(if (listening) Momo.Point else Momo.Ink)
                .clickable(enabled = enabled) { if (listening) Graph.voice.stopListening() else Graph.voice.listen() },
            contentAlignment = Alignment.Center,
        ) { Text("🎙", fontSize = 19.sp, color = Momo.OnAccent) }
    }
}

@Composable
private fun Preview(cs: CreateState, modifier: Modifier, exit: () -> Unit) = Column(modifier) {
    val status by Graph.voice.status.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxWidth().height(190.dp).clip(Round).background(Momo.Surface), contentAlignment = Alignment.Center) {
        when (cs.image) {
            ImageState.NONE -> Text("특징을 다 말하면\n이미지가 만들어져요", fontSize = 14.sp, color = Momo.Sub, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            ImageState.GENERATING -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Momo.Ink)
                Spacer(Modifier.height(11.dp))
                Text("이미지 생성 중…", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("말한 특징으로 모습을 그리고 있어요", fontSize = 13.sp, color = Momo.Sub)
            }
            ImageState.DONE -> Cover(cs.bot, Modifier.fillMaxSize())
        }
    }
    Spacer(Modifier.height(11.dp))

    // 지금 듣고 있는 칸은 첫 번째 빈 칸으로 보여 준다
    val listeningSlot = if (status == VoiceStatus.LISTENING) cs.draft.missing.firstOrNull() else null
    val slots = listOf("캐릭터" to cs.draft.character, "말투" to cs.draft.tone, "세계관" to cs.draft.world, "목소리" to cs.draft.voice)
    slots.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pair.forEach { (label, value) -> Slot(label, value, label == listeningSlot, Modifier.weight(1f)) }
        }
    }
    val bot = cs.bot
    Text("역할 · " + (bot?.roles?.joinToString(", ")?.plus(" (자동)") ?: "확정하면 자동으로 만들어요"), fontSize = 13.sp, color = if (bot != null) Momo.Ink else Momo.Sub)
    Text("시작 설정 · " + (bot?.minsLabel?.plus(" (자동)") ?: "경로 시간별로 자동 생성"), fontSize = 13.sp, color = if (bot != null) Momo.Ink else Momo.Sub)

    Spacer(Modifier.weight(1f))
    if (bot == null) {
        val generating = cs.image == ImageState.GENERATING
        MainButton(
            if (generating) "만드는 중…" else "이대로 만들기 (이미지 생성)", Modifier.fillMaxWidth(),
            enabled = cs.draft.complete && !cs.busy && !generating,
        ) { Graph.creator.confirm() }
        if (!cs.draft.complete) Text("칸 4개가 다 차면 만들 수 있어요", Modifier.padding(top = 7.dp).align(Alignment.CenterHorizontally), fontSize = 12.sp, color = Momo.Sub)
    } else {
        Text(
            "↻ 이미지 다시 만들기", Modifier.align(Alignment.End).clickable(enabled = cs.image != ImageState.GENERATING) { Graph.creator.regenerateImage() }
                .padding(vertical = 5.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium,
        )
        MainButton("이 봇으로 이야기 시작", Modifier.fillMaxWidth()) { val id = bot.id; exit(); AppStore.selectBot(id, Screen.PLAYERS) }
        Spacer(Modifier.height(8.dp))
        MainButton("마켓에 올리기", Modifier.fillMaxWidth(), dark = false) { val id = bot.id; exit(); goUpload(id) }
        Text("만든 봇은 마이페이지에 자동 저장돼요", Modifier.padding(top = 7.dp).align(Alignment.CenterHorizontally), fontSize = 12.sp, color = Momo.Sub)
    }
}

@Composable
private fun Slot(label: String, value: String?, listening: Boolean, modifier: Modifier) = Column(
    modifier.height(70.dp).clip(RoundedCornerShape(12.dp)).background(Momo.Surface)
        .then(if (listening) Modifier.border(1.dp, Momo.Ink, RoundedCornerShape(12.dp)) else Modifier)
        .padding(horizontal = 12.dp, vertical = 9.dp),
    verticalArrangement = Arrangement.Bottom,
) {
    Text(
        value ?: if (listening) "듣는 중…" else "빈 칸", fontSize = 14.sp, maxLines = 2,
        fontWeight = if (value != null) FontWeight.Bold else FontWeight.Normal,
        color = if (value != null || listening) Momo.Ink else Momo.Sub,
    )
    Text(label, fontSize = 12.sp, color = Momo.Sub)
}
