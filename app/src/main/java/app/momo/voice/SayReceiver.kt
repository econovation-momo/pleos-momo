package app.momo.voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.momo.Graph
import app.momo.car.PleosCarSource
import kotlinx.coroutines.launch

/**
 * 시연 백업: 에뮬레이터 마이크가 안 될 때 adb로 사용자 발화를 넣는다.
 *   adb shell am broadcast -a app.momo.SAY --es text "어서 도망가!" -p app.momo
 * TTS 확인용: --es speak "첫 문장.|둘째 문장." 처럼 |로 나누면 문장마다 따로 speak를 불러 순서 보장을 확인한다.
 * STT 확인용: --ez listen true 를 보내면 마이크 인식을 시작한다.
 * 내비 확인용: --ez route true 를 보내면 실제 내비에 공식 문서 예제 목적지로 경로를 요청한다.
 * 매니페스트에서 DUMP 권한을 요구하므로 adb shell만 보낼 수 있고 다른 앱은 보낼 수 없다.
 */
class SayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Graph.init(context.applicationContext)
        intent.getStringExtra("speak")?.split('|')?.forEach { sentence ->
            Graph.scope.launch { Graph.voice.speak(sentence.trim()) }
        }
        if (intent.getBooleanExtra("route", false)) (Graph.car as? PleosCarSource)?.requestSampleRoute()
        if (intent.getBooleanExtra("listen", false)) Graph.voice.listen()
        val text = intent.getStringExtra("text")?.trim().orEmpty()
        if (text.isNotEmpty()) Graph.voice.inject(text)
        else if (intent.extras?.isEmpty != false) Log.w("MomoVoice", "SAY 브로드캐스트에 text가 없습니다")
    }
}
