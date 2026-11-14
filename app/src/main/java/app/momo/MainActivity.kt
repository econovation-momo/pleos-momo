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
        permissions.launch(arrayOf("android.car.permission.CAR_SPEED", "android.permission.RECORD_AUDIO"))
        setContent { MomoTheme { MomoApp() } }
    }
}
