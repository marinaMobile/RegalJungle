package com.tiramisu.driftm.blck

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.appsflyer.AppsFlyerLib
import com.orhanobut.hawk.Hawk
import com.tiramisu.driftm.R
import com.tiramisu.driftm.blck.CNST.DEV
import com.tiramisu.driftm.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var bindMain: ActivityMainBinding
    private val viewModel: ViewModel by viewModels()
    var checker: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)

        setContentView(bindMain.root)

        viewModel.deePP(this)
        val job = GlobalScope.launch(Dispatchers.IO) {
            checker = getCheckCode(CNST.appsUrl)
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
                .init(CNST.AF_DEV_KEY, viewModel.conversionDataListener, applicationContext)
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
                val hawk1: String? = Hawk.get(CNST.C1)
                if (hawk1 != null) {
                    Log.d("TestInUIHawk", hawk1.toString())
                    toTestGrounds()
                    break
                } else {
                    val hawk1: String? = Hawk.get(CNST.C1)
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

}