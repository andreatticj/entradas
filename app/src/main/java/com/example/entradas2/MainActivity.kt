package com.example.entradas2

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Actividad principal de la aplicación que permite configurar una URL y una palabra clave
 * para buscar en una página web periódicamente. Proporciona la funcionalidad para iniciar y
 * detener la tarea de fondo que realiza la búsqueda.
 *
 * <p>Esta actividad utiliza `WorkManager` para manejar tareas en segundo plano. Los valores
 * de URL y palabra clave se guardan dinámicamente en `SharedPreferences` para ser utilizados
 * por el `WebCheckerWorker`.</p>
 *
 * @see WebCheckerWorker
 */
class MainActivity : AppCompatActivity() {

    // Etiqueta única para identificar los trabajos programados
    private val workTag = "WebCheckerWork"

    // ID del trabajo en ejecución, generado dinámicamente
    private var workId = UUID.randomUUID()
    private var semaforo = "R"

    /**
     * Metodo que se ejecuta al crear la actividad.
     *
     * <p>Configura los permisos necesarios, inicializa las vistas y configura los listeners
     * para los botones de inicio y parada de la tarea de fondo. También permite al usuario
     * introducir dinámicamente la URL y la palabra clave mediante campos de texto.</p>
     *
     * @param savedInstanceState Estado previamente guardado de la actividad (si lo hay).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Solicitar permisos para notificaciones si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Inicializar campos de texto
        val urlField = findViewById<EditText>(R.id.url)
        val wordField = findViewById<EditText>(R.id.palabra)

        // Inicializar botones
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnHelp = findViewById<Button>(R.id.btnHelp)
        btnHelp.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            startActivity(intent)
        }

        // Listener para el botón "Play"
        btnPlay.setOnClickListener {
            this.semaforo = "V"
            val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putString("semaforo", semaforo).apply()

            WorkManager.getInstance(this).cancelAllWorkByTag(this.workTag)
            val workRequest = PeriodicWorkRequestBuilder<WebCheckerWorker>(
                15, // Intervalo mínimo de 15 minutos
                TimeUnit.MINUTES
            ).addTag(this.workTag).build()

            // Encolar el trabajo periódico
            WorkManager.getInstance(this).enqueue(workRequest)

            // Guardar el ID del trabajo en ejecución
            this.workId = workRequest.id
        }

        // Listener para el botón "Stop"
        btnStop.setOnClickListener {
            println("Pulso boton stop")
            this.semaforo = "R"
            val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putString("semaforo", semaforo).apply()

            stopWork()
        }

        // Listener para capturar cambios en el campo de texto de la URL
        urlField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se realiza ninguna acción antes del cambio de texto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Se puede manejar el texto mientras cambia (opcional)
            }

            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("url", url).apply()
                println("URL ingresada: $url")
            }
        })

        // Listener para capturar cambios en el campo de texto de la palabra clave
        wordField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se realiza ninguna acción antes del cambio de texto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Se puede manejar el texto mientras cambia (opcional)
            }

            override fun afterTextChanged(s: Editable?) {
                val word = s.toString()
                val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("word", word).apply()
                println("Palabra ingresada: $word")
            }
        })
    }

    /**
     * Metodo para detener la tarea de fondo y cerrar la aplicación.
     *
     * <p>Este mttodo cancela todos los trabajos asociados con la etiqueta o el ID
     * almacenado. También elimina la notificación y finaliza la aplicación de manera controlada.</p>
     */
    private fun stopWork() {
        // Cancelar trabajos asociados con la etiqueta
        WorkManager.getInstance(this).cancelAllWorkByTag(this.workTag)

        // Cancelar trabajos específicos por ID
        WorkManager.getInstance(this).cancelWorkById(this.workId)

        // Eliminar la notificación
        removeNotification()

        // Imprimir información de los trabajos para depuración
        WorkManager.getInstance(this).getWorkInfosByTag(workTag).get().forEach { workInfo ->
            println("Trabajo ID: ${workInfo.id}, Estado: ${workInfo.state}")

            // Cancelar solo si el trabajo está encolado o en ejecución
            if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                WorkManager.getInstance(this).cancelWorkById(workInfo.id)
                println("Trabajo con ID ${workInfo.id} cancelado")
            }
        }
    }

    /**
     * Metodo para eliminar la notificación.
     */
    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1) // Usa el mismo ID de notificación que se usó para crearla
    }
}