package com.example.app_22_testmapviewpager2.main

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.app_22_testmapviewpager2.R





private val TAB_TITLES = arrayOf(
    R.string.tab_text_2,
    R.string.tab_text_1
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var mCurrentFragment: Fragment? = null

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a SearchMapFragment (defined as a static inner class below).
        return when(position) {
            0 -> ApplicationMapFragment()
            else -> ListFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (mCurrentFragment !== `object`) {
            mCurrentFragment = `object` as Fragment
            if (mCurrentFragment is ListFragment)
                (mCurrentFragment as ListFragment).initRecycler()
            else if (mCurrentFragment is ApplicationMapFragment) {
                    (mCurrentFragment as ApplicationMapFragment).refreshMap()
            }
        }
        super.setPrimaryItem(container, position, `object`)
    }
}