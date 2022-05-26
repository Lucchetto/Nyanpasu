package com.zhenxiang.nyaa.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhenxiang.nyaa.CreateTrackerActivity
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.DataSourceSpecs
import com.zhenxiang.nyaa.ext.collectInLifecycle
import com.zhenxiang.nyaa.ext.latestValue
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

private const val ARG_DATASOURCE = "dataSource"
private const val ARG_USERNAME = "username"
private const val ARG_LATEST_TIMESTAMP = "latestTimestamp"

class ReleaseTrackerBottomFragment : BottomSheetDialogFragment() {
    private var dataSource by Delegates.notNull<ApiDataSource>()
    private lateinit var username: String
    private var latestTimestamp by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().let {
            dataSource = it.getSerializable(ARG_DATASOURCE) as ApiDataSource
            username = it.getString(ARG_USERNAME)!!
            latestTimestamp = it.getLong(ARG_LATEST_TIMESTAMP)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val releasesTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)
        val releaseTrackerFragmentSharedViewModel = ViewModelProvider(requireActivity()).get(ReleaseTrackerFragmentSharedViewModel::class.java)

        val fragmentView = inflater.inflate(R.layout.fragment_release_tracker_bottom, container, false)
        val trackAllFromUser = fragmentView.findViewById<View>(R.id.track_all_from_user)
        val trackAllTitle = fragmentView.findViewById<TextView>(R.id.track_all_title)
        val trackAllDesc = fragmentView.findViewById<TextView>(R.id.track_all_desc)
        trackAllFromUser.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (releaseTrackerFragmentSharedViewModel.currentUserTracked.latestValue == true) {
                    releasesTrackerViewModel.deleteTrackedUser(username)
                    releaseTrackerFragmentSharedViewModel.currentUserTracked.emit(false)
                } else {
                    val newTracked = SubscribedTracker(
                        dataSourceSpecs = DataSourceSpecs(dataSource, dataSource.categories[0]),
                        username = username,
                        latestReleaseTimestamp = latestTimestamp,
                        createdTimestamp = System.currentTimeMillis())
                    releasesTrackerViewModel.addReleaseTracker(newTracked)

                    releaseTrackerFragmentSharedViewModel.currentUserTracked.emit(true)
                    dismiss()
                }
            }
        }

        releaseTrackerFragmentSharedViewModel.currentUserTracked.collectInLifecycle(viewLifecycleOwner) {
            trackAllTitle.text = getString(if (it) R.string.untrack_all_from_user_title else R.string.track_all_from_user_title)
            trackAllDesc.text = getString(if (it) R.string.untrack_all_from_user_desc else R.string.track_all_from_user_desc)
        }

        val keywordsTrackerForUser = fragmentView.findViewById<View>(R.id.track_by_keywords)
        keywordsTrackerForUser.setOnClickListener {
            val intent = Intent(activity, CreateTrackerActivity::class.java)
            intent.putExtra(CreateTrackerActivity.PRESET_USERNAME, username)
            intent.putExtra(CreateTrackerActivity.PRESET_DATA_SOURCE, dataSource.value)
            startActivity(intent)
            dismiss()
        }
        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance(dataSource: ApiDataSource, username: String, latestTimestamp: Long) =
            ReleaseTrackerBottomFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATASOURCE, dataSource)
                    putString(ARG_USERNAME, username)
                    putLong(ARG_LATEST_TIMESTAMP, latestTimestamp)
                }
            }
    }
}

// Shared between activity and fragment
class ReleaseTrackerFragmentSharedViewModel : ViewModel() {
    // First value must be set by parent activity
    val currentUserTracked = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )
}

