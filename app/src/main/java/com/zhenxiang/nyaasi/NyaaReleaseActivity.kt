package com.zhenxiang.nyaasi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.tiagohm.markdownview.MarkdownView
import br.tiagohm.markdownview.css.ExternalStyleSheet
import br.tiagohm.markdownview.css.styles.Github
import com.zhenxiang.nyaasi.api.NyaaReleasePreviewItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NyaaReleaseActivity : AppCompatActivity() {

    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_release)

        val nyaaRelease = intent.getSerializableExtra(RELEASE_INTENT_OBJ) as NyaaReleasePreviewItem?

        nyaaRelease?.let {
            val releaseTitle = findViewById<TextView>(R.id.release_title)
            releaseTitle.text = it.name

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val doc: Document = Jsoup.connect("https://nyaa.si/view/${it.id}").get()
                    doc.outputSettings().prettyPrint(false)
                    val descriptionMarkdown = doc.getElementById("torrent-description").html()

                    withContext(Dispatchers.Main) {
                        val markdownView = findViewById<MarkdownView>(R.id.release_details_markdown)
                        markdownView.addStyleSheet(Github())
                        markdownView.loadMarkdown(descriptionMarkdown)

                        val debug = findViewById<TextView>(R.id.debug_stuff)
                        debug.text =descriptionMarkdown
                    }
                } catch(e: Exception) {
                    Log.w(TAG, e)
                }
            }
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