package app.momo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import app.momo.ui.MomoApp
import app.momo.ui.MomoTheme

class MainActivity : ComponentActivity() {
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        Graph.car.start() // CAR_SPEED는 허용 뒤에 다시 구독해야 한다
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.init(this)
        // 주행 여부는 권한이 필요 없는 CarUxRestrictions로 판단한다. CAR_SPEED는 AAOS에서 위치 권한으로 묻기 때문에 요청하지 않는다
        permissions.launch(arrayOf("android.permission.RECORD_AUDIO"))
        setContent { MomoTheme { MomoApp() } }
    }
}
