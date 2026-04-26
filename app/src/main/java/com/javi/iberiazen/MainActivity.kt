package com.javi.iberiazen

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // <--- AÑADIDO
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    // Tasa por defecto (se actualizará con la API)
    private var exchangeRate: Double = 164.50

    private lateinit var etEuro: EditText
    private lateinit var etJpy: EditText
    private lateinit var tvJpyResult: TextView
    private lateinit var tvEurResult: TextView
    private lateinit var tvRate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // 0. Instalar Splash Screen ANTES de super.onCreate
        installSplashScreen() // <--- AÑADIDO

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicializar vistas
        etEuro = findViewById(R.id.etEuro)
        etJpy = findViewById(R.id.etJpy)
        tvJpyResult = findViewById(R.id.tvJpyResult)
        tvEurResult = findViewById(R.id.tvEurResult)
        tvRate = findViewById(R.id.tvRate)

        // 2. Cargar tasa de cambio en tiempo real
        fetchExchangeRate()

        // 3. Listener para EUROS -> YENES
        etEuro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etEuro.isFocused) { // Solo si el usuario está escribiendo aquí
                    val input = s.toString()
                    if (input.isNotEmpty()) {
                        val euros = input.toDoubleOrNull() ?: 0.0
                        val yenes = euros * exchangeRate
                        tvJpyResult.text = "¥ ${String.format("%,.2f", yenes)}"
                    } else {
                        tvJpyResult.text = "¥ 0.00"
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 4. Listener para YENES -> EUROS
        etJpy.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etJpy.isFocused) { // Solo si el usuario está escribiendo aquí
                    val input = s.toString()
                    if (input.isNotEmpty()) {
                        val yenes = input.toDoubleOrNull() ?: 0.0
                        val euros = yenes / exchangeRate
                        tvEurResult.text = "${String.format("%,.2f", euros)} €"
                    } else {
                        tvEurResult.text = "0.00 €"
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @SuppressLint("DefaultLocale")
    private fun fetchExchangeRate() {
        // Ponemos el mensaje que pediste antes de empezar
        tvRate.text = getString(R.string.actualizando_datos)

        thread {
            try {
                // API gratuita y rápida (ExchangeRate-API)
                val response = URL("https://open.er-api.com/v6/latest/EUR").readText()
                val rate = response.substringAfter("\"JPY\":").substringBefore(",").toDouble()

                runOnUiThread {
                    exchangeRate = rate
                    // Actualizamos el mensaje inferior con la tasa real obtenida
                    tvRate.text = getString(R.string.tasa_de_mercado_1, String.format("%.2f", rate))
                }
            } catch (_: Exception) {
                runOnUiThread {
                    tvRate.text = getString(R.string.error_de_red_usando_tasa_guardada)
                }
            }
        }
    }
}