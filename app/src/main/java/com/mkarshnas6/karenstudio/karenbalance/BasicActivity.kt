import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val locale = Locale("en")
        val config = Configuration(newBase?.resources?.configuration)

        Locale.setDefault(locale)
        config.setLocale(locale)

        config.fontScale = 1.0f

        super.attachBaseContext(newBase?.createConfigurationContext(config))
    }
}
