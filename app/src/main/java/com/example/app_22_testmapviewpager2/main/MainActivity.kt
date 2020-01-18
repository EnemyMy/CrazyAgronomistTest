package com.example.app_22_testmapviewpager2.main

import android.content.Intent
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.app_22_testmapviewpager2.R
import com.example.app_22_testmapviewpager2.architecture.ApplicationViewModel
import com.example.app_22_testmapviewpager2.architecture.Discussions
import com.example.app_22_testmapviewpager2.architecture.DiscussionsEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.*

const val ACCESS_FINE_LOCATION_REQUEST_CODE = 123

class MainActivity : AppCompatActivity(), ApplicationMapFragment.OnApplicationMapFragmentInteractionListener, ListFragment.OnListFragmentInteractionListener {

    private lateinit var viewModel : ApplicationViewModel
    private lateinit var discussions : LiveData<DiscussionsEntity>
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    private var location = Location("emptyLocation")

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // Styling elements
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.viewpager)
        viewPager.adapter = sectionsPagerAdapter
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val tabs = (tabLayout.getChildAt(0) as ViewGroup)
        val params = tabLayout.layoutParams
        params.height = 120
        tabLayout.layoutParams = params
        tabLayout.setupWithViewPager(viewPager)
        bottom_navigation[0].findViewById<View>(R.id.menu_item_empty).isClickable = false

        when(tabLayout.selectedTabPosition) {
            0 -> tabs.getChildAt(0).setBackgroundResource(R.drawable.tab_background_selected_first)
            tabLayout.tabCount - 1 -> tabs.getChildAt(tabLayout.tabCount - 1).setBackgroundResource(
                R.drawable.tab_background_selected_last
            )
            else -> tabs.getChildAt(tabLayout.selectedTabPosition).setBackgroundResource(R.drawable.tab_background_selected_middle)
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) = tabs.getChildAt(p0!!.position).setBackgroundColor(resources.getColor(android.R.color.transparent))

            override fun onTabSelected(p0: TabLayout.Tab?) {
                when(p0?.position) {
                    0 -> tabs.getChildAt(0).setBackgroundResource(R.drawable.tab_background_selected_first)
                    tabLayout.tabCount - 1 -> tabs.getChildAt(tabLayout.tabCount - 1).setBackgroundResource(
                        R.drawable.tab_background_selected_last
                    )
                    else -> tabs.getChildAt(p0!!.position).setBackgroundResource(R.drawable.tab_background_selected_middle)
                }
            }
        })

        //Init viewModel
        this.viewModel = ViewModelProviders.of(this).get(ApplicationViewModel::class.java)

        this.discussions = viewModel.getDiscussions()
        this.discussions.observe(this, Observer {
            if (it != null && it.discussions.isNotEmpty() && sectionsPagerAdapter.mCurrentFragment is ApplicationMapFragment) {
                val discussionsList: Array<Discussions> = it.discussions
                val applicationMapFragment = sectionsPagerAdapter.mCurrentFragment as ApplicationMapFragment
                applicationMapFragment.prepareMarkers(discussionsList)
            }
            else {
                if(sectionsPagerAdapter.mCurrentFragment is ApplicationMapFragment)
                    (sectionsPagerAdapter.mCurrentFragment as ApplicationMapFragment).refreshMap(true, clearMarkers = true)
            }
        })
        getCurrentLocation()

    }

    /**
     *
     * Moving camera to our current location with 50km radius
     */

    override fun onInitCurrentLocation(googleMap: GoogleMap) {
        if (location.provider != "emptyLocation") {
            val locationCoords = LatLng(location.latitude, location.longitude)
            val circle = googleMap.addCircle(CircleOptions().center(locationCoords).radius(50000.0).strokeColor(Color.TRANSPARENT))
            circle.isVisible = true
            val radius = circle.radius
            val scale = radius / 500
            val zoomLevel = (16 - ln(scale) / ln(2.0)).toInt()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationCoords, zoomLevel.toFloat()))
        }
        else {
            Toast.makeText(this, "Не вдаеться отримати поточну локацію", Toast.LENGTH_LONG).show()
        }
    }

    /**
     *
     * Sending request and updating our db with info about available discussions in current visible area
     */

    override fun onRequestingMarkers(googleMap: GoogleMap) {
        val progressBar = (sectionsPagerAdapter.mCurrentFragment as ApplicationMapFragment).progressBar
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
        val visibleRegion = googleMap.projection.visibleRegion
        val regionCenter = visibleRegion.latLngBounds.center
        val topMidpoint = LatLngBounds.builder().include(visibleRegion.farLeft).include(visibleRegion.farRight).build().center
        val bottomMidpoint = LatLngBounds.builder().include(visibleRegion.nearLeft).include(visibleRegion.nearRight).build().center

        val radiusArray = FloatArray(1)
        Location.distanceBetween(topMidpoint.latitude, topMidpoint.longitude, bottomMidpoint.latitude, bottomMidpoint.longitude, radiusArray)
        viewModel.updateDiscussions(regionCenter.latitude.toFloat(), regionCenter.longitude.toFloat(), (radiusArray[0] / 2000).toInt())
    }

    /**
     *
     * Searching in LiveData for information about target marker
     */

    override fun onRequestMarkerInfo(position: LatLng): Discussions? {
        var requiredDiscussion : Discussions? = null
        discussions.value?.discussions?.forEach {
            if (it.latitude.toDouble() == position.latitude && it.longitude.toDouble() == position.longitude)
                requiredDiscussion = it
        }
        return requiredDiscussion
    }

    /**
     *
     * Sending current visible discussions to ListFragment
     */

    override fun onRequestCurrentDiscussions() : DiscussionsEntity? = discussions.value

    @AfterPermissionGranted(ACCESS_FINE_LOCATION_REQUEST_CODE)
    private fun getCurrentLocation() {
        viewModel.getCurrentLocation(this, location)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE)
            getCurrentLocation()
    }
}
