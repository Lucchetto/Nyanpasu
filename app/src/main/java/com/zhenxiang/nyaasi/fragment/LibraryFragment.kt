package com.zhenxiang.nyaasi.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.zhenxiang.nyaasi.R

class LibraryFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_library, container, false)

        val appBar = fragmentView.findViewById<AppBarLayout>(R.id.app_bar)
        val tabLayout = fragmentView.findViewById<TabLayout>(R.id.library_tabs)
        val viewPager = fragmentView.findViewById<ViewPager>(R.id.library_pager)
        val viewPagerAdapter = LibraryPagerAdapter(childFragmentManager, viewPager.context)
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            var currentPage = 0
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (currentPage != position && positionOffset < 0.1f) {
                    appBar.setExpanded(true, true)
                    currentPage = position
                }
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LibraryFragment().apply {
            }
    }
}

class LibraryPagerAdapter(fragmentManager: FragmentManager, private val context: Context) :
    FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SavedReleasesFragment.newInstance()
            1 -> ViewedReleasesFragment.newInstance()
            else -> {
                return SavedReleasesFragment.newInstance()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.saved_fragment_bottom_nav)
            1 -> context.getString(R.string.recently_viewed_fragment_bottom_nav)
            else -> {
                return context.getString(R.string.saved_fragment_bottom_nav)
            }
        }
    }
}