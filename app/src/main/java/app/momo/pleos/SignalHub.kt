package app.momo.pleos

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 모든 SDK 신호가 모이는 곳. 키는 "출처/이름" 형식이고, 화면은 출처별로 묶어서 보여 준다.
 * 실제 기능 화면도 이 값을 구독해서 쓰면 된다.
 */
object SignalHub {
    data class Signal(val value: String, val at: Long = System.currentTimeMillis())

    private val _signals = MutableStateFlow<Map<String, Signal>>(emptyMap())
    val signals: StateFlow<Map<String, Signal>> = _signals

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log

    private val time = SimpleDateFormat("HH:mm:ss", Locale.KOREA)

    fun put(key: String, value: Any?) {
        val text = value?.toString() ?: "null"
        val changed = _signals.value[key]?.value != text
        _signals.update { it + (key to Signal(text)) }
        if (changed) log("$key = $text")
    }

    fun fail(key: String, e: Throwable) = put(key, "실패: ${e.javaClass.simpleName} ${e.message ?: ""}")

    fun log(message: String) {
        Log.d("PleosCheck", message)
        _log.update { (listOf("${time.format(Date())}  $message") + it).take(100) }
    }
}
