package com.example.weathersearch

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private var txtTitulo: TextView? = null
    private var txtPesquisar: TextInputLayout? = null
    private var img: ImageView? = null
    private var editPesquisar: TextInputEditText? = null
    private var btnPesquisar: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtTitulo = findViewById(R.id.txtTitulo)
        txtPesquisar = findViewById(R.id.txtPesquisar)
        img = findViewById(R.id.img)
        editPesquisar = findViewById(R.id.editPesquisar)
        btnPesquisar = findViewById(R.id.btnPesquisar)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val cidadePref = preferences.getString("pref_cidade", "n/a")
        if (cidadePref != "n/a") {
            pesquisar(cidadePref)
        }

        val textoPesquisa = editPesquisar?.editableText.toString()
        btnPesquisar?.setOnClickListener { this.pesquisar(textoPesquisa) }
    }

    private fun pesquisar(textoPesquisa: String?) {
        sharedPreferences(textoPesquisa)

        val url =
            "https://dataservice.accuweather.com/locations/v1/search?apikey=ftQ0I21PuLw3yAjhAkJx0ZQg5OSPXJod&language=pt-br&q=$textoPesquisa&details=false"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERRO", e.toString())
            }
            override fun onResponse(call: Call, response: Response) {
                val jsonArray = JSONArray(response.body()?.string())
                val obj: JSONObject = jsonArray.getJSONObject(0)
                val key = obj.get("Key")

                Log.e("KEY_CITY", key.toString())

                pesquisarCidade(key.toString())
            }
        })
    }

    private fun pesquisarCidade(key: String?) {
        val url =
            "https://dataservice.accuweather.com/currentconditions/v1/$key?apikey=ftQ0I21PuLw3yAjhAkJx0ZQg5OSPXJod&language=pt-BR"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERRO", e.toString())
            }
            override fun onResponse(call: Call, response: Response) {
                val jsonArray = JSONArray(response.body()?.string())

                popularCard(jsonArray)
            }
        })
    }

    private fun popularCard(jsonArray: JSONArray) {
        val obj: JSONObject = jsonArray.getJSONObject(0)
        val dataAtual = obj.get("LocalObservationDateTime")
        val textoTempo = obj.get("WeatherText")
        val temPrecipitacao = obj.get("HasPrecipitation")
        val tipoPrecipitacao = obj.get("PrecipitationType")
        val eDia = obj.get("IsDayTime")
        val temperatura: JSONObject = obj.getJSONObject("Temperature").getJSONObject("Metric").getJSONObject("Value")

        Log.e("DATA_CITY", obj.toString())

        //TODO criar layout para card e popular
    }

    private fun sharedPreferences(key: String?) {
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val edit = preferences.edit()

            edit.putString("pref_cidade", key)
            edit.apply()
        } catch (e: Exception) {
            Log.e("PREF_LOG", "ERRO: " + e.message)
        }
    }
}