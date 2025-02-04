package com.example.entradas2

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Actividad que carga un archivo HTML en un WebView.
 * Esta clase muestra un archivo HTML desde los recursos 'assets' en la pantalla del dispositivo Android.
 * Se habilita JavaScript si el archivo HTML lo requiere.
 */
class WebViewActivity : AppCompatActivity() {

    /**
     * Metodo que se llama cuando la actividad es creada.
     * Configura el WebView, habilita JavaScript y carga el archivo HTML desde la carpeta 'assets'.
     *
     * @param savedInstanceState El estado previo de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        // Obtiene la referencia al WebView desde el layout
        val webView = findViewById<WebView>(R.id.webView)

        // Configura un WebViewClient para manejar la navegación dentro del WebView
        webView.webViewClient = WebViewClient()

        // Configura los ajustes del WebView
        val webSettings: WebSettings = webView.settings

        // Habilita JavaScript si el archivo HTML lo requiere
        webSettings.javaScriptEnabled = true

        // Carga el archivo HTML desde los recursos 'assets' del proyecto
        webView.loadUrl("file:///android_asset/web_checker_explanation.html")
    }
}
