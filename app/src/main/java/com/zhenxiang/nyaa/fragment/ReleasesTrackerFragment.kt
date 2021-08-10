package com.zhenxiang.nyaa.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.zhenxiang.nyaa.CreateTrackerActivity
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.ReleaseTrackerDetailsActivity
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import com.zhenxiang.nyaa.releasetracker.SubscribedTrackersAdapter

class ReleasesTrackerFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_releases_tracker, container, false)

        val releaseTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)
        val addBtn = fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.add_btn)
        addBtn.setOnClickListener {
            val intent = Intent(activity, CreateTrackerActivity::class.java)
            startActivity(intent)
        }
        val trackedKeywordsList = fragmentView.findViewById<RecyclerView>(R.id.keywords_trackers_list)
        trackedKeywordsList.layoutManager = LinearLayoutManager(fragmentView.context)
        val trackedKeywordsAdapter = SubscribedTrackersAdapter()
        trackedKeywordsList.adapter = trackedKeywordsAdapter
        //val subscribedUsersAdapter = SubscribedTrackersAdapter()
        releaseTrackerViewModel.subscribedTrackers.observe(viewLifecycleOwner, {
            trackedKeywordsAdapter.setData(it)
        })

        trackedKeywordsAdapter.listener = object: SubscribedTrackersAdapter.ItemClickedListener {
            override fun itemClicked(item: SubscribedTracker) {
                ReleaseTrackerDetailsActivity.startReleaseTrackerDetailsActivity(item, requireActivity())
            }
        }

        /*val subscribedUsersList = fragmentView.findViewById<RecyclerView>(R.id.subscribed_users_list)
        subscribedUsersList.layoutManager = LinearLayoutManager(fragmentView.context)
        subscribedUsersList.adapter = subscribedUsersAdapter*/

        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReleasesTrackerFragment().apply {
            }
    }
}