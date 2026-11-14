package app.momo.engine

import app.momo.R
import app.momo.core.BotStatus
import app.momo.core.Phase
import app.momo.core.Player
import app.momo.core.SeatPos
import app.momo.core.SeedBots
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacingTest {
    @Test fun sceneCountIsRemainMinutesHalved() {
        assertEquals(22, Pacing.sceneCount(45 * 60))
        assertEquals(7, Pacing.sceneCount(15 * 60))
        assertEquals(1, Pacing.sceneCount(60))
    }

    @Test fun sceneIndexAdvancesEveryTwoMinutes() {
        val n = Pacing.sceneCount(45 * 60)
        assertEquals(0, Pacing.sceneIndex(n, 45 * 60))
        assertEquals(2, Pacing.sceneIndex(n, 40 * 60))
        assertEquals(n - 1, Pacing.sceneIndex(n, 0))
    }

    @Test fun resyncOnlyWhenMoreThanThreeMinutesOff() {
        assertFalse(Pacing.needsResync(1800, 1700))
        assertTrue(Pacing.needsResync(1800, 1500))
        assertEquals(5 + 10, Pacing.resyncedCount(5, 20 * 60))
    }

    @Test fun phaseFollowsProgressAndEndsInLastFiveMinutes() {
        assertEquals(Phase.OPENING, Pacing.phase(45 * 60, 0, 22, 0, 5))
        assertEquals(Phase.RISING, Pacing.phase(30 * 60, 7, 22, 1, 5))
        assertEquals(Phase.CLIMAX, Pacing.phase(10 * 60, 17, 22, 4, 5))
        assertEquals(Phase.ENDING, Pacing.phase(300, 19, 22, 4, 5))
    }

    @Test fun chapterRunsAtMostOneAheadOfTimeByTurns() {
        assertEquals(0, Pacing.chapter(5, 0, 22, turns = 2, ending = false))
        assertEquals(1, Pacing.chapter(5, 0, 22, turns = 9, ending = false))
        assertEquals(4, Pacing.chapter(5, 0, 22, turns = 0, ending = true))
        assertEquals(0, Pacing.chapter(0, 3, 22, turns = 9, ending = false))
    }
}

class SafetyFilterTest {
    @Test fun kidsBlocksHarmfulButStoryWordsPass() {
        assertFalse(SafetyFilter.unsafe("용이 피자 먹었으면 좋겠어!", kids = true))
        assertFalse(SafetyFilter.unsafe("칼로 거미줄을 잘라 버리자", kids = true))
        assertFalse(SafetyFilter.unsafe("시간 문 열고 들어가자", kids = true))
        assertTrue(SafetyFilter.unsafe("다 죽여 버려", kids = true))
        assertFalse(SafetyFilter.unsafe("다 죽여 버려", kids = false))
    }

    @Test fun carDangerIsBlockedEvenOutsideKids() {
        assertTrue(SafetyFilter.unsafe("창문 열고 손 내밀어 봐!", kids = false))
        assertTrue(SafetyFilter.unsafe("안전벨트를 풀고 일어나자", kids = false))
        assertEquals(SafetyFilter.SAFE_LINE, SafetyFilter.cleanOutput("머리를 내밀어 봐!", kids = true))
        assertEquals("가자!", SafetyFilter.cleanOutput("가자!", kids = true))
    }
}

class SlotsTest {
    @Test fun parsesJsonFromChattyLlmOutput() {
        val d = Slots.parseJson("""네! {"name": "콩이", "character": "하얀 강아지", "tone": null, "world": "놀이터", "voice": "null"} 입니다""")!!
        assertEquals("콩이 · 하얀 강아지", d.character)
        assertNull(d.tone)
        assertEquals("놀이터", d.world)
        assertNull(d.voice)
        assertNull(Slots.parseJson("모르겠어요"))
    }

    /** 웹 데모의 콩이 QA 흐름이 LLM 없이 그대로 재현되는지 */
    @Test fun webDemoKongFlowWithHeuristicOnly() {
        var d = BotDraft()
        fun answer(text: String): String {
            val before = d
            d = d.merge(Slots.heuristic(text, d.missing.firstOrNull()))
            return Slots.question(d, before)
        }
        assertEquals("좋아요, 말투까지 알았어요! 콩이는 어떤 세계에서 모험할까요?", answer("우리 강아지 콩이! 하얗고 겁 많은데, 멍멍하면서 존댓말 써."))
        assertEquals("콩이 · 하얀 강아지, 겁 많음", d.character)
        assertEquals("“멍멍, 제가 찾아볼게요!” 존댓말", d.tone)
        assertEquals("마지막으로, 목소리는 어떤 느낌이 좋아요?", answer("우리 동네 놀이터가 탐정 사무소야."))
        assertEquals("놀이터 탐정 사무소", d.world)
        assertTrue(answer("밝은 아이 목소리로!").endsWith("이대로 만들까요?"))
        assertEquals("밝은 아이 목소리", d.voice)
        assertTrue(d.complete)

        val bot = Slots.buildBot(d, 1L)
        assertEquals("콩이", bot.character.name)
        assertEquals(listOf("조수 탐정", "사진 담당"), bot.roles)
        assertEquals(listOf(15, 45), bot.minutes)
        assertEquals(R.drawable.cover_kong, bot.cover)
        assertEquals(BotStatus.PRIVATE, bot.status)
        assertEquals("나", bot.creator)
    }

    @Test fun coverKeywordsDoNotMatchInsideWords() {
        assertEquals(R.drawable.cover_dragon, Slots.cover("아기 용이 사는 구름 왕국"))
        assertEquals(R.drawable.cover_dino, Slots.cover("용감한 공룡"))
        assertEquals(R.drawable.cover_space, Slots.cover("로봇 우주 기지"))
        assertEquals(R.drawable.cover_bugs, Slots.cover("장수풍뎅이 올림픽"))
    }

    @Test fun oneUtteranceCanFillSeveralSlotsAndTargetSlotAlwaysFills() {
        val d = Slots.heuristic("용감한 고양이 나비, 반말로 말해", "캐릭터")
        assertEquals("반말", d.tone)
        assertTrue(d.character!!.contains("고양이"))
        assertEquals("바닷속 해적선", Slots.heuristic("바닷속 해적선이야!", "세계관").world)
    }
}

class ScriptTest {
    private val players = listOf(Player(SeatPos.REAR_LEFT, "지우", "기사"), Player(SeatPos.REAR_RIGHT, "지호", "마법사"))

    private fun cue(heard: String?, next: Player?, turn: Int, phase: Phase = Phase.OPENING, final: Boolean = false) = Cue(
        bot = SeedBots.dragon, players = players, next = next, phase = phase, chapter = 0, sceneIndex = 0, sceneCount = 22,
        remainSec = 2700, turn = turn, heard = heard, heardBy = "지우", redirect = false, kids = true, destination = "할머니 댁", final = final,
    )

    @Test fun dragonBranchesOnPizzaAndCallsNextPlayer() {
        val s = ScriptedLlm()
        val open = s.line(cue(null, players[0], 0))
        assertTrue(open, open.contains("지우 기사님"))
        val next = s.line(cue("용이 피자 먹었으면!", players[1], 1).copy(chapter = 1))
        assertTrue(next, next.contains("피자") && next.contains("지호 마법사님"))
        assertTrue(next, splitSentences(next).size <= 3 && next.length <= 120)
    }

    @Test fun unexpectedWordsAreEchoedAndFinalMentionsDestination() {
        val s = ScriptedLlm()
        s.line(cue(null, players[0], 0))
        val echo = s.line(cue("공주님 성에 놀러 가자", players[1], 1))
        assertTrue(echo, echo.contains("공주님 성에 놀러 가자"))
        assertTrue(s.line(cue(null, null, 5, final = true)).contains("할머니 댁"))
    }

    @Test fun genericScriptUsesCharacterName() {
        val line = ScriptedLlm().line(cue(null, players[0], 0).copy(bot = SeedBots.space))
        assertTrue(line, line.contains("삐삐") && line.contains("지우 기사님"))
    }

    @Test fun cleanLineStripsSpeakerAndStageDirections() {
        assertEquals("안녕, 나는 루루야~", cleanLine("루루: (꼬리를 흔들며) 안녕, 나는 루루야~", "루루"))
        assertEquals("“피자”? 좋다!", cleanLine("“피자”? 좋다!", "루루"))
        assertEquals(listOf("우와!", "가자.", "지우 기사님, 어때?"), splitSentences("우와! 가자. 지우 기사님, 어때?"))
    }
}

class RegressionTest {
    /** 통합 검증 2-4: '도착 5분 전으로' 재계산 뒤에도 시간이 지나면 장면 번호가 올라가야 한다 */
    @org.junit.Test fun sceneIndexAdvancesAfterResync() {
        val n = Pacing.resyncedCount(0, 298)
        org.junit.Assert.assertEquals(0, Pacing.sceneIndex(n, 298))
        org.junit.Assert.assertTrue(Pacing.sceneIndex(n, 100) > 0)
    }

    /** 통합 검증 2-1: 목소리로 분류된 말은 지금 묻는 칸(세계관)에 들어가면 안 된다 */
    @org.junit.Test fun voiceAnswerDoesNotFillWorld() {
        val d = Slots.heuristic("밝은 아이 목소리로!", "세계관")
        org.junit.Assert.assertNull(d.world)
        org.junit.Assert.assertNotNull(d.voice)
    }
}
