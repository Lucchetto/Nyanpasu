package com.zhenxiang.nyaa.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhenxiang.nyaa.CreateTrackerActivity
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

private const val ARG_USERNAME = "username"
private const val ARG_LATEST_TIMESTAMP = "latestTimestamp"

class ReleaseTrackerBottomFragment : BottomSheetDialogFragment() {
    private lateinit var username: String
    private var latestTimestamp by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().let {
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
                if (releaseTrackerFragmentSharedViewModel.currentUserTracked.value == true) {
                    releasesTrackerViewModel.deleteTrackedUser(username)
                    withContext(Dispatchers.Main) {
                        releaseTrackerFragmentSharedViewModel.currentUserTracked.value = false
                    }
                } else {
                    val newTracked = SubscribedTracker(username = username,
                        latestReleaseTimestamp = latestTimestamp,
                        createdTimestamp = System.currentTimeMillis())
                    releasesTrackerViewModel.addReleaseTracker(newTracked)
                    withContext(Dispatchers.Main) {
                        releaseTrackerFragmentSharedViewModel.currentUserTracked.value = true
                    }
                    dismiss()
                }
            }
        }

        releaseTrackerFragmentSharedViewModel.currentUserTracked.observe(viewLifecycleOwner) {
            trackAllTitle.text = getString(if (it) R.string.untrack_all_from_user_title else R.string.track_all_from_user_title)
            trackAllDesc.text = getString(if (it) R.string.untrack_all_from_user_desc else R.string.track_all_from_user_desc)
        }

        val keywordsTrackerForUser = fragmentView.findViewById<View>(R.id.track_by_keywords)
        keywordsTrackerForUser.setOnClickListener {
            val intent = Intent(activity, CreateTrackerActivity::class.java)
            intent.putExtra(CreateTrackerActivity.PRESET_USERNAME, username)
            startActivity(intent)
            dismiss()
        }
        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance(username: String, latestTimestamp: Long) =
            ReleaseTrackerBottomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USERNAME, username)
                    putLong(ARG_LATEST_TIMESTAMP, latestTimestamp)
                }
            }
    }
}

// Shared between activity and fragment
class ReleaseTrackerFragmentSharedViewModel : ViewModel() {
    // First value must be set by parent activity
    val currentUserTracked = MutableLiveData<Boolean>()
}

