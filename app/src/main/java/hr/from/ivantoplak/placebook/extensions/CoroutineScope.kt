package hr.from.ivantoplak.placebook.extensions

import android.util.Log
import kotlinx.coroutines.CoroutineScope

fun CoroutineScope.logCoroutine(methodName: String) {
    Log.d(
        "CoroutineLog",
        "Thread for $methodName is: ${Thread.currentThread().name} and the context is: $coroutineContext"
    )
}