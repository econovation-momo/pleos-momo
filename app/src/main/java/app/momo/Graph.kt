package app.momo

import android.content.Context
import app.momo.car.CarSource
import app.momo.car.SimCarSource
import app.momo.engine.BotCreator
import app.momo.engine.LlmClient
import app.momo.engine.PleosLlm
import app.momo.engine.StoryEngine
import app.momo.voice.TextVoice
import app.momo.voice.Voice
import kotlinx.coroutines.MainScope

/**
 * 앱 전역 객체 연결. 세션별로 자기 줄만 고친다.
 * - 차량·음성 세션: car, voice
 * - 엔진 세션: llm
 */
object Graph {
    val scope = MainScope()
    val sim = SimCarSource()

    lateinit var car: CarSource
    lateinit var voice: Voice
    lateinit var llm: LlmClient
    lateinit var engine: StoryEngine
    lateinit var creator: BotCreator

    fun init(context: Context) {
        if (::engine.isInitialized) return

        car = sim

        voice = TextVoice()

        llm = PleosLlm(context)

        car.start()
        voice.start()
        engine = StoryEngine(llm, voice, car, scope)
        creator = BotCreator(llm, scope)
    }
}
