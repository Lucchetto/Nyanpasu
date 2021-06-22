package com.zhenxiang.nyaasi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.zhenxiang.nyaasi.api.NyaaPageProvider
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import com.zhenxiang.nyaasi.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaasi.releasetracker.SubscribedTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTrackerActivity : AppCompatActivity() {

    private val TAG = javaClass.name

    private enum class Status {
        TO_VALIDATE,
        LOADING,
        VALIDATED,
        VALIDATED_EMPTY,
        FAILED,
        FAILED_ALREADY_EXISTS,
    }

    private lateinit var createBtn: Button
    private lateinit var hintText: TextView
    private lateinit var errorHint: TextView
    private lateinit var finishHint: TextView
    private lateinit var loading: View
    private lateinit var latestReleasesList: RecyclerView

    private var currentStatus = Status.TO_VALIDATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tracker)

        val username = intent.getStringExtra(PRESET_USERNAME)
        val releasesTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)

        hintText = findViewById(R.id.hint_text)
        errorHint = findViewById(R.id.error_hint)
        finishHint = findViewById(R.id.finish_hint)
        loading = findViewById(R.id.loading)

        latestReleasesList = findViewById(R.id.last_releases_list)
        val listLayoutManager = LinearLayoutManager(this)
        val latestReleasesAdapter = ReleasesListAdapter(false)
        latestReleasesList.layoutManager = listLayoutManager
        latestReleasesList.adapter = latestReleasesAdapter

        // Initially this button will be validate and not enabled
        createBtn = findViewById(R.id.create_btn)
        val searchQueryInput = findViewById<TextInputEditText>(R.id.search_query_input)
        val usernameInput = findViewById<TextInputEditText>(R.id.username_input)

        username?.let {
            usernameInput.setText(it)
            searchQueryInput.requestFocus()
        } ?: run {
            usernameInput.requestFocus()
        }

        searchQueryInput.doOnTextChanged { text, start, before, count ->
            setStatus(Status.TO_VALIDATE)
            createBtn.isEnabled = text?.isNotBlank() == true
        }

        usernameInput.doOnTextChanged { text, start, before, count ->
            createBtn.isEnabled = searchQueryInput.text?.isNotBlank() == true
            setStatus(Status.TO_VALIDATE)
        }

        createBtn.setOnClickListener { _ ->
            if (currentStatus == Status.TO_VALIDATE) {
                setStatus(Status.LOADING)
                lifecycleScope.launch(Dispatchers.IO) {
                    val username = usernameInput.text.toString().trim()
                    val searchQuery = searchQueryInput.text.toString().trim()
                    releasesTrackerViewModel.getTrackedByUsernameAndQuery(username, searchQuery)?.let {
                        withContext(Dispatchers.Main) {
                            setStatus(Status.FAILED_ALREADY_EXISTS)
                        }
                    } ?: run {
                        val releases = NyaaPageProvider.getPageItems(0, searchQuery = searchQuery, user = username)
                        withContext(Dispatchers.Main) {
                            if (releases == null) {
                                setStatus(Status.FAILED)
                            } else if (releases.isEmpty()) {
                                setStatus(Status.VALIDATED_EMPTY)
                            } else {
                                // Hax to hide keyboard
                                searchQueryInput.clearFocus()
                                usernameInput.clearFocus()
                                val imm: InputMethodManager =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(searchQueryInput.windowToken, 0)

                                latestReleasesAdapter.setItems(releases)
                                setStatus(Status.VALIDATED)
                            }
                        }
                    }
                }
            } else if (currentStatus == Status.VALIDATED) {
                lifecycleScope.launch(Dispatchers.IO) {
                    latestReleasesAdapter.getItems().getOrNull(0)?.let {
                        val username = usernameInput.text.toString().trim()
                        val searchQuery = searchQueryInput.text.toString().trim()
                        val newTracker = SubscribedTracker(username = if (username.isBlank()) null else username,
                            searchQuery = searchQuery, lastReleaseTimestamp = it.timestamp)
                        releasesTrackerViewModel.addReleaseTracker(newTracker)
                        finish()
                    }
                }
            } else if (currentStatus == Status.VALIDATED_EMPTY) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val username = usernameInput.text.toString().trim()
                    val searchQuery = searchQueryInput.text.toString().trim()
                    // Current millis must be divided by 1000 since nyaa.si use seconds as unit
                    val newTracker = SubscribedTracker(username = if (username.isBlank()) null else username,
                        searchQuery = searchQuery, lastReleaseTimestamp = System.currentTimeMillis() / 1000)
                    releasesTrackerViewModel.addReleaseTracker(newTracker)
                    finish()
                }
            }
        }

        val cancelBtn = findViewById<Button>(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            finish()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore saved bundle
        savedInstanceState?.let {
            setStatus(it.getSerializable("currentState") as Status)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("currentState", currentStatus)
        super.onSaveInstanceState(outState)
    }

    private fun setStatus(status: Status) {
        if (status == currentStatus) {
            return
        }
        if (BuildConfig.DEBUG) {
            Log.w(TAG, status.toString())
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
        latestReleasesList.visibility = if (status == Status.VALIDATED) View.VISIBLE else View.GONE

        if (status != currentStatus && (status == Status.TO_VALIDATE || currentStatus == Status.TO_VALIDATE || status == Status.LOADING || currentStatus == Status.LOADING)) {
            createBtn.text = getString(if (status == Status.TO_VALIDATE || status == Status.LOADING) R.string.validate else R.string.create)
        }

        if (status == Status.FAILED || status == Status.FAILED_ALREADY_EXISTS) {
            errorHint.visibility = View.VISIBLE
            when (status) {
                Status.FAILED -> errorHint.text = getString(R.string.tracker_validation_failed_error)
                Status.FAILED_ALREADY_EXISTS -> errorHint.text = getString(R.string.tracker_already_exists_error)
            }
        } else {
            errorHint.visibility = View.GONE
        }

        if (status != Status.TO_VALIDATE) {
            createBtn.isEnabled = status != Status.FAILED && status != Status.FAILED_ALREADY_EXISTS
        }
        currentStatus = status
    }

    companion object {
        const val PRESET_USERNAME = "presetUsername"
    }
}