package com.zhenxiang.nyaasi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        val browseFragment: Fragment
        val savedFragment: Fragment

        if (savedInstanceState == null) {
            browseFragment = setupFragment(BrowseFragment.newInstance(), "1")
            savedFragment = setupFragment(ViewedReleasesFragment.newInstance(), "2")
            switchActiveFragment(browseFragment)
        } else {
            browseFragment = supportFragmentManager.findFragmentByTag("1")!!
            savedFragment = supportFragmentManager.findFragmentByTag("2")!!
        }

        bottomNav = findViewById(R.id.bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, null)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.browseFragment -> switchActiveFragment(browseFragment)
                R.id.savedFragment -> switchActiveFragment(savedFragment)
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