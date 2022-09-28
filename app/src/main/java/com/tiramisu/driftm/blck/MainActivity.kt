package com.tiramisu.driftm.blck

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.orhanobut.hawk.Hawk
import com.tiramisu.driftm.AppS.Companion.AF_DEV_KEY
import com.tiramisu.driftm.AppS.Companion.C1
import com.tiramisu.driftm.AppS.Companion.D1
import com.tiramisu.driftm.AppS.Companion.DEV
import com.tiramisu.driftm.AppS.Companion.appsUrl
import com.tiramisu.driftm.R
import com.tiramisu.driftm.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var bindMain: ActivityMainBinding

    var checker: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)

        setContentView(bindMain.root)

        deePP(this)

        val job = GlobalScope.launch(Dispatchers.IO) {
            checker = getCheckCode(appsUrl)
            Log.d("CHECKAPPS", "I did something")
        }
        runBlocking {
            try {
                job.join()
            } catch (_: Exception){
            }
        }

        val prefs = getSharedPreferences("ActivityPREF", MODE_PRIVATE)
        if (prefs.getBoolean("activity_exec", false)) {
            Intent(this, Filt::class.java).also { startActivity(it) }
            finish()
        } else {
            val exec = prefs.edit()
            exec.putBoolean("activity_exec", true)
            exec.apply()
        }
        Log.d("DevChecker", isDevMode(this).toString())
        Hawk.put(DEV, isDevMode(this).toString())

        if (checker){
            AppsFlyerLib.getInstance()
                .init(AF_DEV_KEY, conversionDataListener, applicationContext)
            AppsFlyerLib.getInstance().start(this)
            afNullRecordedOrNotChecker(1500)
            Log.d("AppsChecker", "Apps works")
        } else {
            Log.d("AppsChecker", "Apps doesn't work")
            toTestGrounds()
            Toast.makeText(this, "GOOD", Toast.LENGTH_SHORT).show()
        }


    }



    private suspend fun getCheckCode(link: String): Boolean {
        val url = URL(link)
        val urlConnection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection

        return try {
            val text = urlConnection.inputStream.bufferedReader().readText()
            if (text == "1") {
                Log.d("jsoup status", text)
                true
            } else {
                Log.d("jsoup status", "is null")
                false
            }
        } finally {
            urlConnection.disconnect()
        }

    }

    private fun afNullRecordedOrNotChecker(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            while (NonCancellable.isActive) {
                val hawk1: String? = Hawk.get(C1)
                if (hawk1 != null) {
                    Log.d("TestInUIHawk", hawk1.toString())
                    toTestGrounds()
                    break
                } else {
                    val hawk1: String? = Hawk.get(C1)
                    Log.d("TestInUIHawkNulled", hawk1.toString())
                    delay(timeInterval)
                }
            }
        }
    }
    private fun toTestGrounds() {
        Intent(this, Filt::class.java)
            .also { startActivity(it) }
        finish()
    }
    private fun isDevMode(context: Context): Boolean {
        return run {
            Settings.Secure.getInt(context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
        }
    }

    val conversionDataListener = object : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {

            val dataGotten = data?.get("campaign").toString()
//                val dataGotten = "apps_sub2_sub3_sub4"
            Hawk.put(C1, dataGotten)
            Log.d("devTEST", data.toString())
        }

        override fun onConversionDataFail(p0: String?) {

        }

        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

        }

        override fun onAttributionFailure(p0: String?) {
        }
    }


    fun deePP(context: Context) {
        AppLinkData.fetchDeferredAppLinkData(
            context
        ) { appLinkData: AppLinkData? ->
            appLinkData?.let {
                val params = appLinkData.targetUri.host

                Log.d("D11PL", "$params")
//                val conjoined = TextUtils.join("/", params)
//                Log.d("FB_TEST:", conjoined)

                Hawk.put(D1, params.toString())


            }
            if (appLinkData == null) {
                Log.d("FB_ERR:", "Params = null")
            }
        }
    }

}