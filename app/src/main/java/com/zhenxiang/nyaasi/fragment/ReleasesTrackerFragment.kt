package com.zhenxiang.nyaasi.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.zhenxiang.nyaasi.CreateTrackerActivity
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaasi.releasetracker.SubscribedTrackersAdapter

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
        val trackedKeywordsCount = fragmentView.findViewById<TextView>(R.id.query_trackers_count)
        val trackedKeywordsList = fragmentView.findViewById<RecyclerView>(R.id.keywords_trackers_list)
        trackedKeywordsList.layoutManager = LinearLayoutManager(fragmentView.context)
        val trackedKeywordsAdapter = SubscribedTrackersAdapter()
        trackedKeywordsList.adapter = trackedKeywordsAdapter
        //val subscribedUsersAdapter = SubscribedTrackersAdapter()
        releaseTrackerViewModel.subscribedTrackers.observe(viewLifecycleOwner, {
            trackedKeywordsCount.text = it.size.toString()
            trackedKeywordsAdapter.setData(it)
        })

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