package hr.from.ivantoplak.placebook.coroutines

import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {

    fun main(): CoroutineContext

    fun io(): CoroutineContext

    fun default(): CoroutineContext
}