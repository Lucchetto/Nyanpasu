package com.zhenxiang.nyaasi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.api.NyaaViewModel
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [BrowseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrowseFragment : Fragment() {

    private lateinit var viewModel: NyaaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val downloadsAdapter = DownloadsAdapter()
        viewModel = ViewModelProvider(this).get(NyaaViewModel::class.java)
        viewModel.itemsLiveData.observe(viewLifecycleOwner,  {
            downloadsAdapter.setItems(it)
        })

        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_browse, container, false)

        val downloadsList = fragmentView.findViewById<RecyclerView>(R.id.downloads_list)
        downloadsList.layoutManager = LinearLayoutManager(fragmentView.context)
        downloadsList.adapter = downloadsAdapter

        lifecycleScope.launch() {
            viewModel.loadData()
        }
        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BrowseFragment().apply {
            }
    }
}