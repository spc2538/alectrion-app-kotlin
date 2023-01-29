package com.project.alectrion_app_kotlin.utils

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.project.alectrion_app_kotlin.firebase.NotificationJson
import java.io.*
import java.lang.StringBuilder

class MainApplication : Application() {
    var storagePacket: StoragePacket = StoragePacket

    override fun onCreate() {
        super.onCreate()
        val stringJson = readFromFile(applicationContext())
        val gson = Gson()
        if(stringJson == ""){
            val jsonString = gson.toJson(storagePacket.settingsJson, StorageJson::class.java)
            writeToFile(jsonString, applicationContext())
        }else {
            storagePacket.settingsJson = gson.fromJson(stringJson, StorageJson::class.java)
        }
    }

    fun setName(remoteMessage: String, context: Context){
        val gson = Gson()
        val jsonData = gson.fromJson(remoteMessage, NotificationJson::class.java)
        storagePacket.settingsJson.name = jsonData.status
        val jsonString = gson.toJson(storagePacket.settingsJson, StorageJson::class.java)
        writeToFile(jsonString, context)
    }

    fun setEnvironment (environment: String, context: Context){
        val gson = Gson()
        storagePacket.settingsJson.environment = environment
        val jsonString = gson.toJson(storagePacket.settingsJson, StorageJson::class.java)
        writeToFile(jsonString, context)
    }

    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    private fun writeToFile(data: String, context: Context){
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(storagePacket.fileName, MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException){
            println("Exception: File write failed: $e")
        }
    }

    private fun readFromFile(context: Context): String? {
        var ret = ""
        try {
            val inputStream: InputStream = context.openFileInput(storagePacket.fileName)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String? = ""
            val stringBuilder = StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append(receiveString)
            }
            inputStream.close()
            ret = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            Log.e("main activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("main activity", "Can not read file: $e")
        }
        return ret
    }
}
