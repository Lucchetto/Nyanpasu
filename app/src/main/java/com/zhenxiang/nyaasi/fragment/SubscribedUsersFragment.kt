package com.zhenxiang.nyaasi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaasi.releasetracker.SubscribedTrackersAdapter

class SubscribedUsersFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_subscribed_users, container, false)

        val releaseTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)
        val subscribedUsersAdapter = SubscribedTrackersAdapter()
        releaseTrackerViewModel.subscribedTrackers.observe(viewLifecycleOwner, {
            subscribedUsersAdapter.setData(it)
        })

        val subscribedUsersList = fragmentView.findViewById<RecyclerView>(R.id.subscribed_users_list)
        subscribedUsersList.layoutManager = LinearLayoutManager(fragmentView.context)
        subscribedUsersList.adapter = subscribedUsersAdapter

        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SubscribedUsersFragment().apply {
            }
    }
}