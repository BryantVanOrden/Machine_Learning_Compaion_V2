package np.com.bryant.myapp.util

import android.content.Context
import android.widget.Toast
import java.security.AccessControlContext

object UiUtil {
    fun showToast(context: Context, message : String){
        Toast.makeText(context,message, Toast.LENGTH_LONG).show()
    }
}