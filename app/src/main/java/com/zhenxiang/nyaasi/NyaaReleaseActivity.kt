package com.zhenxiang.nyaasi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import br.tiagohm.markdownview.MarkdownView
import br.tiagohm.markdownview.css.styles.Github
import com.zhenxiang.nyaasi.db.NyaaRelease
import com.zhenxiang.nyaasi.api.NyaaReleasePreviewItem
import com.zhenxiang.nyaasi.db.NyaaDb
import com.zhenxiang.nyaasi.view.ReleaseDataItemView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.DateFormat

class NyaaReleaseActivity : AppCompatActivity() {

    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_release)

        val scrollRoot = findViewById<NestedScrollView>(R.id.scroll_root)
        scrollRoot.isNestedScrollingEnabled = false

        val nyaaRelease = intent.getSerializableExtra(RELEASE_INTENT_OBJ) as NyaaReleasePreviewItem?

        nyaaRelease?.let {
            val releaseTitle = findViewById<TextView>(R.id.release_title)
            releaseTitle.text = it.name

            val idView = findViewById<TextView>(R.id.release_id)
            idView.text = "ID: ${it.id}"

            val magnetBtn = findViewById<View>(R.id.magnet_btn)
            magnetBtn.setOnClickListener { _ ->
                AppUtils.openMagnetLink(this, it, scrollRoot)
            }

            val category = findViewById<TextView>(R.id.category)
            category.text = getString(R.string.release_category, getString(it.category.stringResId))

            val date = findViewById<TextView>(R.id.date)
            date.text = getString(R.string.release_date,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(it.date))

            val seeders = findViewById<ReleaseDataItemView>(R.id.seeders)
            seeders.setValue(it.seeders.toString())

            val leechers = findViewById<ReleaseDataItemView>(R.id.leechers)
            leechers.setValue(it.leechers.toString())

            val completed = findViewById<ReleaseDataItemView>(R.id.completed)
            completed.setValue(it.completed.toString())

            val releaseSizeView = findViewById<ReleaseDataItemView>(R.id.release_size)
            releaseSizeView.setValue(it.releaseSize)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val doc: Document = Jsoup.connect("https://nyaa.si/view/${it.id}").get()
                    doc.outputSettings().prettyPrint(false)

                    val userName = doc.selectFirst("div.col-md-1:matches(Submitter:)").parent().select("a[href~=^(.*?)\\/user\\/(.+)\$]").text()
                    val hash = doc.selectFirst("div.col-md-1:matches(Info hash:)").parent().select("kbd:matches(^(\\w{40})\$)").text()
                    val descriptionMarkdown = doc.getElementById("torrent-description").html()

                    val release = NyaaRelease(it.id, it.name, it.magnet, it.date, it.seeders,
                        it.leechers, it.completed, it.category, it.releaseSize,
                        if (userName.isNullOrEmpty()) null else userName, hash, descriptionMarkdown)

                    val db = NyaaDb(this@NyaaReleaseActivity)
                    db.nyaaReleasesDao().insert(release)

                    withContext(Dispatchers.Main) {
                        val submitter = findViewById<TextView>(R.id.submitter)
                        submitter.text = getString(R.string.release_submitter,
                            release.user?.let { release.user } ?: run { getString(R.string.submitter_null) })

                        val markdownView = findViewById<MarkdownView>(R.id.release_details_markdown)
                        markdownView.addStyleSheet(Github())
                        markdownView.loadMarkdown(release.descriptionMarkdown)

                        // Hide loading circle
                        findViewById<View>(R.id.progress_frame).visibility = View.GONE
                        findViewById<View>(R.id.release_extra_data).visibility = View.VISIBLE
                        markdownView.visibility = View.VISIBLE
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