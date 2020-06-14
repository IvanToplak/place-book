package hr.from.ivantoplak.placebook.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

inline fun <reified T : Activity> Context.startActivity(key: String, bundle: Bundle) {
    val intent = Intent(this, T::class.java).apply { putExtra(key, bundle) }
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.startActivityForResult(
    key: String,
    bundle: Bundle,
    requestCode: Int
) {
    val intent = Intent(this, T::class.java).apply { putExtra(key, bundle) }
    startActivityForResult(intent, requestCode)
}