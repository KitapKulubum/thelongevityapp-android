package com.thelongevityapp.android.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** §9.3: Emitted when 401 retry fails and user was signed out. */
object AuthEvents {
    private val _invalidated = MutableSharedFlow<Unit>()
    val invalidated: SharedFlow<Unit> = _invalidated

    fun notifyInvalidated() {
        CoroutineScope(Dispatchers.Main).launch { _invalidated.emit(Unit) }
    }
}
