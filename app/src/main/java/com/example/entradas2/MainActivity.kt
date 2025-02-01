package com.example.entradas2

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Actividad principal de la aplicación que permite al usuario configurar y gestionar
 * el monitoreo de una página web en segundo plano. El usuario puede introducir una URL
 * y una palabra clave para buscar en la página. La aplicación notificará al usuario
 * cuando la palabra clave sea encontrada en la página web.
 */
class MainActivity : AppCompatActivity() {

    private val workTag = "WebCheckerWork" // Etiqueta para identificar el trabajo en segundo plano
    private var semaforo = "R" // Controla el estado del Worker ("V" para activo, "R" para detenido)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Solicitar permiso para mostrar notificaciones en dispositivos con Android 13 (TIRAMISU) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // Referencias a los elementos de la interfaz de usuario
        val urlField = findViewById<EditText>(R.id.url)
        val wordField = findViewById<EditText>(R.id.palabra)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnStop = findViewById<Button>(R.id.btnStop)

        // Configurar el botón "Play" para iniciar el monitoreo
        btnPlay.setOnClickListener {
            semaforo = "V" // Activar el semáforo
            val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putString("semaforo", semaforo).apply()

            // Cancelar cualquier trabajo previo y programar uno nuevo
            WorkManager.getInstance(this).cancelAllWorkByTag(workTag)
            val workRequest = PeriodicWorkRequestBuilder<WebCheckerWorker>(15, TimeUnit.MINUTES)
                .addTag(workTag)
                .build()

            WorkManager.getInstance(this).enqueue(workRequest)
        }

        // Configurar el botón "Stop" para detener el monitoreo
        btnStop.setOnClickListener {
            stopWork()
        }

        // Listener para guardar la URL introducida por el usuario en las preferencias compartidas
        urlField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE).edit().putString("url", url)
                    .apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Listener para guardar la palabra clave introducida por el usuario en las preferencias compartidas
        wordField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val word = s.toString()
                getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE).edit().putString("word", word)
                    .apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Detiene el trabajo en segundo plano y elimina la notificación.
     */
    private fun stopWork() {
        semaforo = "R" // Desactivar el semáforo
        val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("semaforo", semaforo).apply()
        WorkManager.getInstance(this).cancelAllWorkByTag(workTag)
        removeNotification()
    }

    /**
     * Elimina la notificación mostrada al usuario cuando se le da a detener/Stop
     */
    private fun removeNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1) // Usa el mismo ID de la notificación que se usó para crearla
    }
}
