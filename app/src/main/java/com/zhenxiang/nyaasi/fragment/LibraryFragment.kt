package com.zhenxiang.nyaasi.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.zhenxiang.nyaasi.R




class LibraryFragment : Fragment() {

    private var searchMode = false
    private lateinit var searchBtn: ExtendedFloatingActionButton
    private lateinit var appBar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var searchBar: SearchView
    private lateinit var toolbarContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_library, container, false)

        toolbar = fragmentView.findViewById(R.id.library_toolbar)
        appBar = fragmentView.findViewById(R.id.app_bar)
        toolbarContainer = fragmentView.findViewById(R.id.toolbar_container)
        searchBtn = fragmentView.findViewById(R.id.search_btn)
        searchBar = fragmentView.findViewById(R.id.search_bar)

        val tabLayout = fragmentView.findViewById<TabLayout>(R.id.library_tabs)

        searchBtn.setOnClickListener {
            setSearchMode(true)
        }

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
                    // Hax to get fragment
                    val fragment = childFragmentManager.findFragmentByTag("android:switcher:${R.id.library_pager}:$position")
                    if (fragment != null && fragment is ViewedReleasesFragment && fragment.listHeight() < viewPager.height - toolbar.height) {
                        appBar.setExpanded(true, true)
                    }
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

    fun setSearchMode(search: Boolean) {
        if (search == searchMode) {
            return
        }
        searchBar.visibility = if (search) View.VISIBLE else View.GONE
        if (search) {
            appBar.setExpanded(true, true)
            searchBar.requestFocus()
            searchBtn.hide()
        } else {
            searchBtn.show()
        }
        val scrollLayoutParams = toolbarContainer.layoutParams as AppBarLayout.LayoutParams
        scrollLayoutParams.scrollFlags = if (search) AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP else AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS.or(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL)
        searchMode = search
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