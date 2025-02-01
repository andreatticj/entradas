package com.example.entradas2

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
class WebCheckerWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        try {
            // Obtener la configuración desde la base de datos
            val databaseHelper = DatabaseHelper(applicationContext)
            val cursor = databaseHelper.getConfig()

// Verifica si el cursor tiene al menos una fila
            if (cursor.count == 0) {
                cursor.close()
                return Result.failure()
            }

            cursor.moveToFirst()

// Verificar si las columnas existen usando getColumnIndex
            val urlIndex = cursor.getColumnIndex(DatabaseHelper.COL_URL)
            val wordIndex = cursor.getColumnIndex(DatabaseHelper.COL_WORD)
            val semaforoIndex = cursor.getColumnIndex(DatabaseHelper.COL_SEMAFORO)

// Comprobar si las columnas existen
            if (urlIndex == -1 || wordIndex == -1 || semaforoIndex == -1) {
                cursor.close()
                return Result.failure()  // Si alguna columna no existe, devuelve un fallo
            }

// Ahora que las columnas son válidas, obtenemos los valores
            val url = cursor.getString(urlIndex)
            val word = cursor.getString(wordIndex)
            val semaforo = cursor.getString(semaforoIndex)

            cursor.close()


            // Verificar si el Worker ha sido detenido o si el semáforo está en "R" (detenido)
            if (isStopped || semaforo == "R") {
                println("Worker detenido por el semáforo")
                return Result.failure()
            }

            // Conectar a la página web y obtener su contenido
            val doc = Jsoup.connect(url).get()
            if (isStopped || semaforo == "R") {
                return Result.failure()
            }

            // Verificar si la palabra clave está presente en el contenido de la página
            if (doc.text().contains(word, ignoreCase = true)) {
                sendNotification() // Enviar notificación si la palabra es encontrada
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
        return Result.success()
    }

    /**
     * Envía una notificación al usuario indicando que la palabra clave fue encontrada.
     */
    private fun sendNotification() {
        val context = applicationContext
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un Intent para detener el Worker cuando el usuario presione la notificación
        val stopIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "STOP_WORK"
        }
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
            ).apply {
                description = "Canal para notificaciones de palabras encontradas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, "guestlist_channel")
            .setContentTitle("¡Se encontró la palabra!")
            .setContentText("La palabra fue encontrada en la página web.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_delete, "Detener", stopPendingIntent)
            .build()

        // Mostrar la notificación
        notificationManager.notify(1, notification)
    }
}