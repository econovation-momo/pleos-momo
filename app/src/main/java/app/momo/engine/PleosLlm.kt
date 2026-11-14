package app.momo.engine

import ai.pleos.playground.llm.LLM
import ai.pleos.playground.llm.constant.Mode
import ai.pleos.playground.llm.listener.ResultListener
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/**
 * Pleos LLM SDK(2.1.3.2) 어댑터. 공식 문서가 내려가 있어서 다른 참가팀의 public 코드에서 확인한 모양만 쓴다:
 * LLM(context, Mode.ON_DEVICE), initialize(), generateContent(prompt, ResultListener{ onResponse(text, completed), onError(reason) }).
 * 매니페스트에 pleos.car.permission.LLM_SERVICE 가 필요하다.
 * onResponse 가 조각(delta)으로 오는지 누적 문자열로 오는지는 문서로 확인하지 못해서 두 경우를 모두 처리한다.
 * SDK에 생성을 멈추는 API가 있는지 몰라서, 취소되면 이후에 오는 콜백을 버리기만 한다.
 */
class PleosLlm(context: Context) : LlmClient {
    override val name = "Pleos LLM"

    private val llm: LLM? = runCatching { LLM(context.applicationContext, Mode.ON_DEVICE).also { it.initialize() } }
        .onSuccess { Log.i(TAG, "초기화 성공") }
        .onFailure { Log.w(TAG, "초기화 실패", it) }
        .getOrNull()

    override fun stream(system: String, prompt: String): Flow<String> = callbackFlow {
        val model = llm ?: throw IllegalStateException("Pleos LLM 초기화 실패")
        val startedAt = System.currentTimeMillis()
        var acc = ""
        model.generateContent("$system\n\n$prompt", object : ResultListener {
            override fun onResponse(response: String, completed: Boolean) {
                if (acc.isEmpty()) Log.i(TAG, "첫 조각 ${System.currentTimeMillis() - startedAt}ms")
                val cumulative = acc.isNotEmpty() && response.startsWith(acc)
                val delta = if (cumulative) response.substring(acc.length) else response
                acc = if (cumulative) response else acc + response
                if (delta.isNotEmpty()) trySend(delta)
                if (completed) {
                    Log.i(TAG, "완료 ${System.currentTimeMillis() - startedAt}ms: ${acc.take(80)}")
                    close()
                }
            }

            override fun onError(reason: String) {
                Log.w(TAG, "오류: $reason")
                close(IllegalStateException(reason))
            }
        })
        awaitClose { }
    }.buffer(Channel.UNLIMITED)

    fun release() { runCatching { llm?.release() } }

    private companion object { const val TAG = "Momo.PleosLlm" }
}
