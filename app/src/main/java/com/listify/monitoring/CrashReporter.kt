package com.listify.monitoring

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporter @Inject constructor() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }

    fun setUserContext(userId: String, email: String) {
        crashlytics.setUserId(userId)
        crashlytics.setCustomKey("email", email)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
