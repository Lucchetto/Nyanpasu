package com.zhenxiang.nyaa

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.api.*
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import dev.chrisbanes.insetter.applyInsetter
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
        FAILED_USER_NOT_FOUND,
        FAILED_ALREADY_EXISTS,
    }

    private lateinit var createBtn: Button
    private lateinit var hintText: TextView
    private lateinit var errorHint: TextView
    private lateinit var finishHint: TextView
    private lateinit var loading: View
    private lateinit var latestReleasesList: RecyclerView
    private lateinit var searchQueryInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var categoriesDropdown: MaterialAutoCompleteTextView
    private lateinit var dataSourcesDropdown: MaterialAutoCompleteTextView

    private lateinit var searchViewModel: DataSourceViewModel

    private var selectedCategoryIndex = -1
    private var selectedDataSourceIndex = -1
    private var currentStatus = Status.TO_VALIDATE

    private val trackerValidator = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            createBtn.isEnabled = !usernameInput.text.isNullOrBlank() || !searchQueryInput.text.isNullOrBlank()
            setStatus(Status.TO_VALIDATE)
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tracker)

        val username = intent.getStringExtra(PRESET_USERNAME)
        val releasesTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)
        searchViewModel = ViewModelProvider(this).get(DataSourceViewModel::class.java)

        hintText = findViewById(R.id.hint_text)
        errorHint = findViewById(R.id.error_hint)
        finishHint = findViewById(R.id.finish_hint)
        loading = findViewById(R.id.loading)

        latestReleasesList = findViewById(R.id.last_releases_list)
        val listLayoutManager = LinearLayoutManager(this)
        val latestReleasesAdapter = ReleasesListAdapter(false)
        latestReleasesList.itemAnimator = null
        latestReleasesList.layoutManager = listLayoutManager
        latestReleasesList.adapter = latestReleasesAdapter

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && NavigationModeUtils.isFullGestures(this)) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            findViewById<View>(R.id.create_tracker_activity_root).applyInsetter {
                type(statusBars = true) {
                    margin()
                }
            }
        }

        // Initially this button will be validate and not enabled
        createBtn = findViewById(R.id.create_btn)
        searchQueryInput = findViewById<TextInputEditText>(R.id.search_query_input)
        usernameInput = findViewById<TextInputEditText>(R.id.username_input)
        categoriesDropdown = findViewById<MaterialAutoCompleteTextView>(R.id.categories_selection)
        dataSourcesDropdown = findViewById<MaterialAutoCompleteTextView>(R.id.data_source_selection)
        // Hax to always show all items, we'll never reach that threshold so filter is never triggered
        categoriesDropdown.threshold = Int.MAX_VALUE
        categoriesDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryIndex = position
        }

        dataSourcesDropdown.threshold = Int.MAX_VALUE
        dataSourcesDropdown.setAdapter(AppUtils.getDataSourcesAdapter(this, false))
        dataSourcesDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedDataSourceIndex = position
            setDataSource(ApiDataSource.values()[position])
        }

        // Prefill username field if necessary
        username?.let {
            usernameInput.setText(it)
        }
        if (savedInstanceState == null) {
            setDataSource(ApiDataSource.NYAA_SI)
            categoriesDropdown.setText(AppUtils.getReleaseCategoryString(this, ApiDataSource.NYAA_SI.categories[0]), false)
            dataSourcesDropdown.setText(ApiDataSource.NYAA_SI.url)
            selectedCategoryIndex = 0
            selectedDataSourceIndex = 0
            searchQueryInput.requestFocus()

            // Run validation on first creation
            createBtn.isEnabled = !usernameInput.text.isNullOrBlank() || !searchQueryInput.text.isNullOrBlank()
        } else {
            selectedCategoryIndex = savedInstanceState.getInt("selectedCategoryIndex")
            selectedDataSourceIndex = savedInstanceState.getInt("selectedDataSourceIndex")
            setDataSource(ApiDataSource.values()[selectedDataSourceIndex])
        }

        searchQueryInput.addTextChangedListener(trackerValidator)
        usernameInput.addTextChangedListener(trackerValidator)
        categoriesDropdown.addTextChangedListener(trackerValidator)

        searchViewModel.resultsLiveData.observe(this, {
            if (it.isEmpty()) {
                setStatus(Status.VALIDATED_EMPTY)
            } else {
                // Hax to hide keyboard
                searchQueryInput.clearFocus()
                usernameInput.clearFocus()
                categoriesDropdown.clearFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchQueryInput.windowToken, 0)

                latestReleasesAdapter.setItems(it)
                setStatus(Status.VALIDATED)
            }
        })
        searchViewModel.error.observe(this, {
            setStatus(
                when(it) {
                    PAGE_NOT_FOUND -> Status.FAILED_USER_NOT_FOUND
                    else -> Status.FAILED
                }
            )
        })

        createBtn.setOnClickListener { _ ->
            val username = formatUsernameOrQueryForTracker(usernameInput.text)
            val searchQuery = formatUsernameOrQueryForTracker(searchQueryInput.text)
            val category = ApiDataSource.values()[selectedDataSourceIndex].categories[selectedCategoryIndex]
            if (currentStatus == Status.TO_VALIDATE) {
                setStatus(Status.LOADING)
                lifecycleScope.launch(Dispatchers.IO) {
                    releasesTrackerViewModel.getTrackerWithSameSpecs(username, searchQuery, category)?.let {
                        withContext(Dispatchers.Main) {
                            setStatus(Status.FAILED_ALREADY_EXISTS)
                        }
                    } ?: run {
                        latestReleasesAdapter.setItems(emptyList())
                        withContext(Dispatchers.Main) {
                            searchViewModel.setSearchText(searchQuery)
                            searchViewModel.setCategory(category)
                            searchViewModel.setUsername(username)
                        }
                        searchViewModel.loadResults()
                    }
                }
            } else if (currentStatus == Status.VALIDATED) {
                lifecycleScope.launch(Dispatchers.IO) {
                    latestReleasesAdapter.getItems().getOrNull(0)?.let {
                        val newTracker = SubscribedTracker(
                            dataSourceSpecs = DataSourceSpecs(category.getDataSource(), category),
                            username = username,
                            searchQuery = searchQuery,
                            latestReleaseTimestamp = it.timestamp,
                            createdTimestamp = System.currentTimeMillis())
                        releasesTrackerViewModel.addReleaseTracker(newTracker)
                        finish()
                    }
                }
            } else if (currentStatus == Status.VALIDATED_EMPTY) {
                lifecycleScope.launch(Dispatchers.IO) {
                    // Current millis must be divided by 1000 since nyaa.si use seconds as unit
                    // Explicitly tell that hasPreviousReleases is false
                    val newTracker = SubscribedTracker(
                        dataSourceSpecs = DataSourceSpecs(category.getDataSource(), category),
                        username = username,
                        searchQuery = searchQuery,
                        latestReleaseTimestamp = System.currentTimeMillis() / 1000,
                        createdTimestamp = System.currentTimeMillis(), hasPreviousReleases = false)
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

    private fun formatUsernameOrQueryForTracker(text: Editable?): String? {
        return if (text.isNullOrBlank()) null else text.toString().trim()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore saved bundle
        setStatus(savedInstanceState.getSerializable("currentState") as Status)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("currentState", currentStatus)
        outState.putInt("selectedCategoryIndex", selectedCategoryIndex)
        outState.putInt("selectedDataSourceIndex", selectedDataSourceIndex)
        super.onSaveInstanceState(outState)
    }

    private fun setDataSource(apiDataSource: ApiDataSource) {
        categoriesDropdown.setAdapter(AppUtils.getCategoriesAdapter(this, apiDataSource, false))
        categoriesDropdown.setText(AppUtils.getReleaseCategoryString(this, apiDataSource.categories[0]), false)
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

        if (status == Status.FAILED || status == Status.FAILED_ALREADY_EXISTS || status == Status.FAILED_USER_NOT_FOUND) {
            errorHint.visibility = View.VISIBLE
            when (status) {
                Status.FAILED -> errorHint.text = getString(R.string.tracker_validation_failed_error)
                Status.FAILED_USER_NOT_FOUND -> errorHint.text = getString(R.string.tracker_validation_user_not_found)
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