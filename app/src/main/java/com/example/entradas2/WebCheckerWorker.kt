package com.example.entradas2

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup

/**
 * Worker que realiza el trabajo en segundo plano de monitorear una página web
 * en busca de una palabra clave específica. Si la palabra es encontrada, se envía
 * una notificación al usuario.
 */
class WebCheckerWorker
/**
 * Constructor del Worker.
 *
 * @param appContext    Contexto de la aplicación.
 * @param workerParams  Parámetros del Worker.
 */
    (appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    /**
     * Método que realiza el trabajo en segundo plano.
     *
     * @return Result.success() si se ejecuta correctamente, Result.failure() en caso de error o si el Worker es detenido.
     */
    override fun doWork(): Result {
        try {
            // Obtener las preferencias compartidas para acceder a la URL, la palabra clave y el semáforo
            val context = applicationContext
            val sharedPreferences =
                context.getSharedPreferences("WebCheckerPrefs", Context.MODE_PRIVATE)
            val semaforo = sharedPreferences.getString("semaforo", null)


            // Verificar si el Worker ha sido detenido o si el semáforo está en "R" (detenido)
            if (isStopped || "R" == semaforo) {
                println("Worker detenido por el semáforo")
                return Result.failure()
            }

            // Obtener la URL y la palabra clave de las preferencias compartidas
            val url = sharedPreferences.getString("url", null)
            val word = sharedPreferences.getString("word", null)

            if (url == null || word == null) {
                return Result.failure()
            }

            // Conectar a la página web y obtener su contenido
            val doc = Jsoup.connect(url).get()
            if (isStopped || "R" == semaforo) {
                return Result.failure()
            }

            // Verificar si la palabra clave está presente en el contenido de la página
            if (doc.text().contains(word)) {
                sendNotification() // Enviar notificación si la palabra es encontrada
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
        return Result.success()
    }

    /**
     * Envía una notificación al usuario indicando que la palabra clave fue encontrada en la página web.
     */
    private fun sendNotification() {
        val context = applicationContext
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un Intent para detener el Worker cuando el usuario presione la notificación
        val stopIntent = Intent(
            context,
            NotificationReceiver::class.java
        )
        stopIntent.setAction("STOP_WORK")
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear un canal de notificación para dispositivos con Android 8.0 (Oreo) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "guestlist_channel",
                "Guestlist Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para notificaciones de palabras encontradas"
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, "guestlist_channel")
            .setContentTitle("¡Se encontró la palabra!")
            .setContentText("La palabra fue encontrada en la página web.")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_delete, "Detener", stopPendingIntent)

        // Mostrar la notificación
        notificationManager.notify(1, notification.build())
    }
}