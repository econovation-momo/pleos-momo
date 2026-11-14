package app.momo.engine

import kotlinx.coroutines.flow.Flow

/**
 * LLM 어댑터. Pleos LLM SDK(PleosLlm)가 기본이고, 실패하면 미리 써 둔 대본(ScriptedLlm)으로 바꾼다.
 * 외부 LLM API도 이 인터페이스만 구현하면 끼울 수 있다.
 */
interface LlmClient {
    val name: String
    /** 응답을 조각(delta) 단위로 흘려보낸다. 끝나면 flow가 닫힌다. */
    fun stream(system: String, prompt: String): Flow<String>
}
