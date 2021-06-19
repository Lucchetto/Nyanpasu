package com.zhenxiang.nyaasi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.zhenxiang.nyaasi.api.NyaaPageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTrackerActivity : AppCompatActivity() {

    private enum class Status {
        TO_VALIDATE,
        LOADING,
        VALIDATED,
        VALIDATED_EMPTY,
        FAILED,
    }

    private lateinit var createBtn: Button
    private lateinit var hintText: TextView
    private lateinit var errorHint: TextView
    private lateinit var finishHint: TextView
    private lateinit var loading: View
    private lateinit var latestReleasesHeader: View
    private lateinit var latestReleasesList: RecyclerView

    private var currentStatus = Status.TO_VALIDATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tracker)

        hintText = findViewById(R.id.hint_text)
        errorHint = findViewById(R.id.error_hint)
        finishHint = findViewById(R.id.finish_hint)
        loading = findViewById(R.id.loading)

        latestReleasesHeader = findViewById(R.id.last_releases_header)
        latestReleasesList = findViewById(R.id.last_releases_list)
        val listLayoutManager = LinearLayoutManager(this)
        val latestReleasesAdapter = ReleasesListAdapter(false)
        latestReleasesAdapter.setFooterVisible(false)
        latestReleasesList.layoutManager = listLayoutManager
        latestReleasesList.adapter = latestReleasesAdapter

        // Initially this button will be validate and not enabled
        createBtn = findViewById(R.id.create_btn)
        val searchQuery = findViewById<TextInputEditText>(R.id.search_query_input)
        val username = findViewById<TextInputEditText>(R.id.username_input)

        searchQuery.doOnTextChanged { text, start, before, count ->
            setStatus(Status.TO_VALIDATE)
            createBtn.isEnabled = text?.isNotEmpty() == true
        }

        username.doOnTextChanged { text, start, before, count ->
            createBtn.isEnabled = searchQuery.text?.isNotEmpty() == true
            setStatus(Status.TO_VALIDATE)
        }

        createBtn.setOnClickListener {
            if (currentStatus == Status.TO_VALIDATE) {
                setStatus(Status.LOADING)
                lifecycleScope.launch(Dispatchers.IO) {
                    val releases = NyaaPageProvider.getPageItems(0, searchQuery = searchQuery.text.toString(), user = username.text.toString())
                    withContext(Dispatchers.Main) {
                        if (releases == null) {
                            setStatus(Status.FAILED)
                        } else if (releases.isEmpty()) {
                            setStatus(Status.VALIDATED_EMPTY)
                        } else {
                            // Hax to hide keyboard
                            searchQuery.clearFocus()
                            username.clearFocus()
                            val imm: InputMethodManager =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(searchQuery.windowToken, 0)

                            latestReleasesAdapter.setItems(releases)
                            setStatus(Status.VALIDATED)
                        }
                    }
                }
            }
        }

        val cancelBtn = findViewById<Button>(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun setStatus(status: Status) {
        if (status == currentStatus) {
            return
        }
        if (status == Status.TO_VALIDATE || status == Status.VALIDATED_EMPTY) {
            hintText.visibility = View.VISIBLE
            when (status) {
                Status.VALIDATED_EMPTY -> hintText.text = getString(R.string.no_releases_found_for_tracker_hint)
                Status.TO_VALIDATE -> hintText.text = getString(R.string.create_tracker_hint)
            }
        } else {
            hintText.visibility = View.GONE
        }
        loading.visibility = if (status == Status.LOADING) View.VISIBLE else View.GONE
        finishHint.visibility = if (status == Status.VALIDATED) View.VISIBLE else View.GONE
        latestReleasesHeader.visibility = if (status == Status.VALIDATED) View.VISIBLE else View.GONE
        latestReleasesList.visibility = if (status == Status.VALIDATED) View.VISIBLE else View.GONE

        if (status != currentStatus && (status == Status.TO_VALIDATE || currentStatus == Status.TO_VALIDATE || status == Status.LOADING || currentStatus == Status.LOADING)) {
            createBtn.text = getString(if (status == Status.TO_VALIDATE || status == Status.LOADING) R.string.validate else R.string.create)
        }

        if (status == Status.FAILED) {
            errorHint.visibility = View.VISIBLE
            when (status) {
                Status.FAILED -> errorHint.text = getString(R.string.tracker_validation_failed_error)
            }
        } else {
            errorHint.visibility = View.GONE
        }

        if (status != Status.TO_VALIDATE) {
            createBtn.isEnabled = status != Status.FAILED
        }
        currentStatus = status
    }
}