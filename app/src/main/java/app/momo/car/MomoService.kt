package app.momo.car

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log

/**
 * 내비가 전체 화면으로 앞에 나와도 앱 프로세스가 얼지 않게 붙잡아 두는 포그라운드 서비스.
 * 하는 일은 없고, 차량 콜백과 TTS·STT가 계속 돌도록 프로세스 우선순위만 올린다.
 * 마이크 타입은 RECORD_AUDIO가 허용된 뒤에만 붙일 수 있어서, 허용 뒤 start()를 한 번 더 부른다.
 */
class MomoService : Service() {
    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "이야기 진행", NotificationManager.IMPORTANCE_LOW))
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("MOMO")
            .setContentText("이야기를 이어 가고 있어요")
            .build()
        val mic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK or
            (if (mic) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(1, notification, type) else startForeground(1, notification)
            Log.i("MomoCar", "포그라운드 서비스 시작 (마이크 타입 ${if (mic) "포함" else "없음"})")
        } catch (e: Exception) {
            Log.w("MomoCar", "포그라운드 서비스 시작 실패", e)
            stopSelf()
        }
        return START_STICKY
    }

    companion object {
        private const val CHANNEL = "story"

        fun start(context: Context) {
            try {
                context.startForegroundService(Intent(context, MomoService::class.java))
            } catch (e: Exception) {
                // 앱이 백그라운드에 있을 때 불리면 Android 12+가 막는다. 다음 화면 진입 때 다시 시도된다.
                Log.w("MomoCar", "포그라운드 서비스 요청 실패", e)
            }
        }
    }
}
