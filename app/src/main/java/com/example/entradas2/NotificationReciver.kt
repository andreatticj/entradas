package com.example.entradas2

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager

/**
* BroadcastReceiver que maneja la acción de detener el Worker cuando el usuario
* presiona la notificación. También elimina la notificación.
*/
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "STOP_WORK") {
            // Detener el Worker y cancelar todas las tareas asociadas
            WorkManager.getInstance(context).cancelAllWorkByTag("WebCheckerWork")

            // Eliminar la notificación
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1) // Usa el mismo ID de la notificación que se usó para crearla
        }
    }
}
