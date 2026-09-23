package com.organistaapp

import android.app.Application
import com.organistaapp.utils.NotificationUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OrganistaApplication : Application() {

    @Inject
    lateinit var notificationUtils: NotificationUtils

    override fun onCreate() {
        super.onCreate()
        notificationUtils.criarCanalNotificacao()
    }
}
