package com.zhenxiang.nyaasi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val doc: Document = Jsoup.connect("https://nyaa.si/").get()
                val pageItems = doc.select("tr.default")
                Log.w("asdasdsa", "${pageItems.size}")
            } catch(e: Exception) {
                Log.e("asd", "exception", e)
            }
        }
    }
}