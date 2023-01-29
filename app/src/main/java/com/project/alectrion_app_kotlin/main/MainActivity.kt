package com.project.alectrion_app_kotlin.main
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.alectrion_app_kotlin.R
import com.project.alectrion_app_kotlin.utils.MainApplication
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import com.project.alectrion_app_kotlin.foreground.Actions
import com.project.alectrion_app_kotlin.foreground.ForegroundService
import com.project.alectrion_app_kotlin.foreground.getServiceState
import com.project.alectrion_app_kotlin.foreground.ServiceState
import okhttp3.*
import java.io.IOException
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var mainApplication: MainApplication
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button
    private lateinit var textWelcome: TextView
    private var clickCount by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainApplication = MainApplication()
        clickCount = 0
        /*
        Si se requiere que la app funcione solamente con el permiso de "sobre-poner otras apps"
        descomentar las siguientes lineas y agregar dependencias (android studio indica cuales)
         */

//        if (!Settings.canDrawOverlays(this)) {
//            val intent =
//                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            startActivityForResult(intent, 0)
//        }

        buttonStart = findViewById<View>(R.id.buttonStart) as Button
        buttonStop = findViewById<View>(R.id.buttonStop) as Button
        textWelcome = findViewById(R.id.welcome_placeholder) as TextView

        buttonStart.setOnClickListener {
            actionOnService(Actions.START)
        }
        buttonStop.setOnClickListener {
            actionOnService(Actions.STOP)
        }
        textWelcome.setOnClickListener {
            clickCount = clickCount + 1
            println(clickCount)
            if (clickCount == 10) {
                if (mainApplication.storagePacket.settingsJson.environment == "productive"){
                    mainApplication.setEnvironment("development", this)
                    Toast.makeText(this,"Entorno:: " + mainApplication.storagePacket.settingsJson.environment,Toast.LENGTH_LONG).show()
                } else {
                    mainApplication.setEnvironment("productive", this)
                    Toast.makeText(this,"Entorno:: " + mainApplication.storagePacket.settingsJson.environment,Toast.LENGTH_LONG).show()
                }
                clickCount = 0
            }
        }
        println("Entorno:: " + mainApplication.storagePacket.settingsJson.environment)
        Log.d("TAG", "Entorno:: " + mainApplication.storagePacket.settingsJson.environment)

        // Realizar peticion http al iniciar actividad
        // requestAlectrion()

        if (mainApplication.storagePacket.settingsJson.environment == "development"){
            runOnUiThread {
                Toast.makeText(this,"Entorno:: " + mainApplication.storagePacket.settingsJson.environment,Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, ForegroundService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                println("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            println("Starting the service in < 26 Mode")
            startService(it)
        }
    }
    private fun requestAlectrion(){
        val request = Request.Builder()
            .url("https://criteria.mx")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                println("Response code: " + response.code)
                runOnUiThread {
                    if(response.code != 200){
                        val statusWelcome: TextView = findViewById(R.id.welcome_placeholder)
                        statusWelcome.text = "Error code host"
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                println("FAILURE" + e.toString())
                val statusWelcome: TextView = findViewById(R.id.welcome_placeholder)
                statusWelcome.text = "Error internet connection or hostname does not exist"
            }
        })
    }

    override fun onResume() {
        super.onResume()
        /*
        Si se requiere que la app funcione solamente con el permiso de "sobre-poner otras apps"
        descomentar las siguientes lineas
         */

//        if (!Settings.canDrawOverlays(this)) {
//            val intent =
//                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            startActivityForResult(intent, 0)
//        }
    }
}