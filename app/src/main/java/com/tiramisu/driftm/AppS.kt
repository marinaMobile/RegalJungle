package com.tiramisu.driftm

import android.app.Application
import android.content.Context
import com.onesignal.OneSignal
import com.orhanobut.hawk.Hawk
import com.tiramisu.driftm.blck.Adv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class gitAppS : Application() {

    companion object {
        const val AF_DEV_KEY = "rHhkgEKwbpptP6p9QyKWk6"
        const val jsoupCheck = "3f1f"
        const val ONESIGNAL_APP_ID = "7dabd7ad-22e0-4cbe-b389-088658e97a7f"

        var lru = "http://regaljungle.xyz/go.php?to=1&"
        var appsUrl = "http://regaljungle.xyz/apps.txt"


        val odone = "sub_id_1="
        val twoSub = "sub_id_2="


        var MAIN_ID: String? = ""
        var C1: String? = "c11"
        var D1: String? = "d11"
        var DEV: String = "false"
    }

    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
        GlobalScope.launch(Dispatchers.IO) {
            applyDeviceId(context = applicationContext)
        }
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)


    }

    private suspend fun applyDeviceId(context: Context) {
        val advertisingInfo = Adv(context)
        val idInfo = advertisingInfo.getAdvertisingId()
        Hawk.put(AppS.MAIN_ID, idInfo)
    }
}