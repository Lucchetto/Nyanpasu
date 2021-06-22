package com.zhenxiang.nyaasi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhenxiang.nyaasi.fragment.BrowseFragment
import com.zhenxiang.nyaasi.fragment.LibraryFragment
import com.zhenxiang.nyaasi.fragment.ReleasesTrackerFragment
import dev.chrisbanes.insetter.applyInsetter

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav : BottomNavigationView
    private var activeFragment : Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        findViewById<View>(R.id.main_root).applyInsetter {
            type(navigationBars = true) {
                padding()
            }

            type(statusBars = true) {
                padding()
            }
        }

        /*findViewById<View>(R.id.fragment_container).applyInsetter {
            type(ime = true) {
                margin()
            }
        }*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, inset ->
            val imeInset = WindowInsetsCompat(inset).getInsets(WindowInsetsCompat.Type.ime()).bottom
            if (imeInset > 0) {
                val navInset = WindowInsetsCompat(inset).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                v.setPadding(0,0,0, imeInset - bottomNav.height - navInset)
            } else {
                v.setPadding(0)
            }

            return@setOnApplyWindowInsetsListener inset
        }

        val browseFragment: Fragment
        val libraryFragment: Fragment
        val subscribedUsersFragment: Fragment

        if (savedInstanceState == null) {
            browseFragment = setupFragment(BrowseFragment.newInstance(), "1")
            libraryFragment = setupFragment(LibraryFragment.newInstance(), "3")
            subscribedUsersFragment = setupFragment(ReleasesTrackerFragment.newInstance(), "4")
            switchActiveFragment(browseFragment)
        } else {
            browseFragment = supportFragmentManager.findFragmentByTag("1")!!
            libraryFragment = supportFragmentManager.findFragmentByTag("3")!!
            subscribedUsersFragment = supportFragmentManager.findFragmentByTag("4")!!
        }

        bottomNav = findViewById(R.id.bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, null)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.browseFragment -> switchActiveFragment(browseFragment)
                R.id.libraryFragment -> switchActiveFragment(libraryFragment)
                R.id.subscribedUsers -> switchActiveFragment(subscribedUsersFragment)
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selectedTab", bottomNav.selectedItemId)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        bottomNav.selectedItemId = savedInstanceState.getInt("selectedTab")
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun setupFragment(fragment : Fragment, title : String) : Fragment {
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment, title).hide(fragment).commit()
        return fragment
    }

    private fun switchActiveFragment(newFragment : Fragment) {
        if (newFragment != activeFragment) {
            val transaction = supportFragmentManager.beginTransaction()
            activeFragment?.let { transaction.hide(it) }
            transaction.show(newFragment).commit()
            activeFragment = newFragment
        }
    }
}