package com.tiramisu.driftm.blck

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.tiramisu.driftm.AppS.Companion.AF_DEV_KEY
import com.tiramisu.driftm.AppS.Companion.C1
import com.tiramisu.driftm.AppS.Companion.D1
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



        val prefs = getSharedPreferences("ActivityPREF", MODE_PRIVATE)
        if (prefs.getBoolean("activity_exec", false)) {
            Intent(this, Filt::class.java).also { startActivity(it) }
            finish()
        } else {
            val exec = prefs.edit()
            exec.putBoolean("activity_exec", true)
            exec.apply()
        }


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

        val sharPref = getSharedPreferences("SP", MODE_PRIVATE)


        return CoroutineScope(Dispatchers.IO).launch {
            while (NonCancellable.isActive) {
                val hawk1: String? = sharPref.getString(C1, null)
                if (hawk1 != null) {
                    Log.d("TestInUIHawk", hawk1.toString())
                    toTestGrounds()
                    break
                } else {
                    val hawk1: String? = sharPref.getString(C1, null)
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

    private val conversionDataListener = object : AppsFlyerConversionListener {



        override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {

            val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
            val editor = sharPref.edit()

            val dataGotten = data?.get("campaign").toString()
//                val dataGotten = "apps_sub2_sub3_sub4"
            editor.putString(C1, dataGotten)
            editor.apply()


            Log.d("devTEST", data.toString())
        }

        override fun onConversionDataFail(p0: String?) {

        }

        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

        }

        override fun onAttributionFailure(p0: String?) {
        }
    }


    private fun deePP(context: Context) {

        val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
        val editor = sharPref.edit()

        AppLinkData.fetchDeferredAppLinkData(
            context
        ) { appLinkData: AppLinkData? ->
            appLinkData?.let {
                val params = appLinkData.targetUri.host

                Log.d("D11PL", "$params")
//                val conjoined = TextUtils.join("/", params)
//                Log.d("FB_TEST:", conjoined)

                editor.putString(D1, params.toString())
                editor.commit()

            }
            if (appLinkData == null) {
                Log.d("FB_ERR:", "Params = null")
            }
        }
    }

}