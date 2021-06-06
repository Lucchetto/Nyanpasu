package com.zhenxiang.nyaasi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zhenxiang.nyaasi.api.NyaaReleasePreviewItem

class NyaaReleaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_release)

        val nyaaRelease = intent.getSerializableExtra(RELEASE_INTENT_OBJ) as NyaaReleasePreviewItem?

        nyaaRelease?.let {

            val releaseTitle = findViewById<TextView>(R.id.release_title)
            releaseTitle.text = it.name
        } ?: run {
            finish()
        }
    }

    companion object {
        const val RELEASE_INTENT_OBJ = "nyaaRelease"

        fun startNyaaReleaseActivity(release: NyaaReleasePreviewItem, activity: Activity) {
            val intent = Intent(activity, NyaaReleaseActivity::class.java).putExtra(RELEASE_INTENT_OBJ, release)
            activity.startActivity(intent)
        }
    }
}