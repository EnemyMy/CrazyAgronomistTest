package com.example.app_22_testmapviewpager2.main


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.example.app_22_testmapviewpager2.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.app_22_testmapviewpager2.architecture.Discussions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 */
class ApplicationMapFragment : Fragment(), OnMapReadyCallback {
    private var listener: OnApplicationMapFragmentInteractionListener? = null

    private lateinit var mapView : View
    private lateinit var googleMap : GoogleMap
    lateinit var progressBar: ProgressBar
    private var lastClickedMarker : Marker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_application_map, container, false)
        mapView = root.findViewById(R.id.supportMap)
        progressBar = root.findViewById(R.id.progressBar)
        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnApplicationMapFragmentInteractionListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnApplicationMapFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        val map : SupportMapFragment = childFragmentManager.findFragmentById(R.id.supportMap) as SupportMapFragment
        map.getMapAsync(this)
    }


    override fun onMapReady(p0: GoogleMap?) {
        if (p0 == null) return
        this.googleMap = p0

        p0.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
            listener?.onInitCurrentLocation(p0)
            listener?.onRequestingMarkers(p0)
        })

        p0.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener {
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            prepareMarker(it)
            true
        })

        //customizing myLocation button
        val locationButton : Button? = view?.findViewById(R.id.myLocationButton)
        locationButton?.setOnClickListener {
            listener?.onInitCurrentLocation(p0)
            listener?.onRequestingMarkers(p0)
        }

        //adding functionality for custom button
        view?.findViewById<Button>(R.id.showDiscussionsForCurrentAreaButton)?.setOnClickListener {
            listener?.onRequestingMarkers(p0)
        }

    }

    /**
     *
     * Getting all available discussions and preparing marker views for holding them
     */

    @SuppressLint("UnsafeExperimentalUsageError")
    fun prepareMarkers(discussions : Array<Discussions>) {
        val customViewMap = mutableMapOf<Int, View>()
        discussions.forEachIndexed { index, discussion ->
            val customMarkerView = LayoutInflater.from(context).inflate(R.layout.custom_marker_collapsed, null)
            customMarkerView.layoutParams = ViewGroup.LayoutParams(35, 35)
            val backgroundView = customMarkerView.findViewById<LinearLayout>(R.id.custom_marker_background)
            val markerImageView = customMarkerView.findViewById(R.id.image) as ShapeableImageView

            setBackgroundForCustomMarker(backgroundView, discussion.problemId)

            markerImageView.shapeAppearanceModel = markerImageView.shapeAppearanceModel
                .toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, dpToPx(10))
                .setTopLeftCorner(CornerFamily.ROUNDED, dpToPx(10))
                .setBottomLeftCorner(CornerFamily.ROUNDED, dpToPx(10))
                .build()

            if (discussion.iconUrl.isNotEmpty()) {
                loadImage("http://app.crazyagro.com/admin/img/${discussion.iconUrl}", 65, markerImageView, object : Callback {
                    override fun onSuccess() {
                        customViewMap[index] = customMarkerView
                        if (customViewMap.size == discussions.size)
                            placeMarkers(discussions, customViewMap)
                    }

                    override fun onError(e: Exception?) {
                        Log.e("prepareMarkers: ", e?.message)
                    }
                })
            }
            else {
                markerImageView.setImageDrawable(resources.getDrawable(R.drawable.img_placeholder))
                customViewMap[index] = customMarkerView
                if (customViewMap.size == discussions.size)
                    placeMarkers(discussions, customViewMap)
            }
        }

    }

    /**
     *
     * Placing prepared markers on the map
     */

    private fun placeMarkers(discussions: Array<Discussions>, customViewMap : MutableMap<Int, View>) {
        refreshMap(true, clearMarkers = true)
        discussions.forEachIndexed { index, discussion ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(discussion.latitude.toDouble(), discussion.longitude.toDouble()))
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(customViewMap[index])))
            )
        }
    }

    /**
     *
     * Converting view to bitmap for placing on GM
     */

    private fun getMarkerBitmapFromView(view : View?): Bitmap {

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        view!!.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidthAndState, view.measuredHeightAndState)

        val returnedBitmap = Bitmap.createBitmap(
            view.measuredWidthAndState, view.measuredHeightAndState,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = view.background
        drawable?.draw(canvas)
        view.draw(canvas)
        return returnedBitmap
    }

    /**
     *
     * Called when user interacts with marker.
     * Method prepares target marker for expand and collapses previous target marker
     */

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun prepareMarker(marker : Marker, isExpanded : Boolean = false) {

        val discussion = listener?.onRequestMarkerInfo(marker.position) ?: return

        if (isExpanded) {
            val customMarkerView = LayoutInflater.from(context).inflate(R.layout.custom_marker_collapsed, null)
            customMarkerView.layoutParams = ViewGroup.LayoutParams(35, 35)
            val backgroundView = customMarkerView.findViewById<LinearLayout>(R.id.custom_marker_background)
            val markerImageView = customMarkerView.findViewById(R.id.image) as ShapeableImageView

            setBackgroundForCustomMarker(backgroundView, discussion.problemId)

            markerImageView.shapeAppearanceModel = markerImageView.shapeAppearanceModel
                .toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, dpToPx(10))
                .setTopLeftCorner(CornerFamily.ROUNDED, dpToPx(10))
                .setBottomLeftCorner(CornerFamily.ROUNDED, dpToPx(10))
                .build()

            if (discussion.iconUrl.isNotEmpty()) {
                loadImage("http://app.crazyagro.com/admin/img/${discussion.iconUrl}", 65, markerImageView, object : Callback {
                    override fun onSuccess() {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(customMarkerView)))
                        marker.zIndex = 0.0f
                    }

                    override fun onError(e: Exception?) {
                        Log.e("prepareMarkers: ", e?.message)
                    }
                })
            }
            else {
                markerImageView.setImageDrawable(resources.getDrawable(R.drawable.img_placeholder))
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(customMarkerView)))
                marker.zIndex = 0.0f
            }
            return
        }

        if (lastClickedMarker != null)
            prepareMarker(lastClickedMarker as Marker, true)

        val customMarkerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
        customMarkerView.layoutParams = ViewGroup.LayoutParams(35, 35)
        val backgroundView = customMarkerView.findViewById<LinearLayout>(R.id.custom_marker_background)
        val markerImageView = customMarkerView.findViewById(R.id.image) as ShapeableImageView
        val markerCulture : TextView = customMarkerView.findViewById(R.id.custom_marker_culture)
        val markerProblem : TextView = customMarkerView.findViewById(R.id.custom_marker_problem)
        val markerStatus : TextView = customMarkerView.findViewById(R.id.custom_marker_status)

        setBackgroundForCustomMarker(backgroundView, discussion.problemId)

        markerImageView.shapeAppearanceModel = markerImageView.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, dpToPx(10))
            .setBottomLeftCorner(CornerFamily.ROUNDED, dpToPx(10))
            .build()

        markerCulture.text = "Культура: ${CULTURE_IDS[discussion.cultureId]}"
        markerProblem.text = "Проблема: ${PROBLEM_IDS[discussion.problemId]}"
        markerStatus.text = when {
            discussion.commentsAmount == 0 -> "Статус: Коментарів нема"
            discussion.commentsAmount == 1 -> "Статус: 1 експертний коментар"
            discussion.commentsAmount in 2..4 -> "Статус: ${discussion.commentsAmount} експертних коментаря"
            else -> "Статус: ${discussion.commentsAmount} експертних коментарів"
        }

        if (discussion.iconUrl.isNotEmpty()) {
            loadImage("http://app.crazyagro.com/admin/img/${discussion.iconUrl}", 75, markerImageView, object : Callback {
                override fun onSuccess() {
                    changeMarker(marker, customMarkerView)
                }

                override fun onError(e: Exception?) {
                    Log.e("prepareMarkers: ", e?.message)
                }
            })
        }
        else {
            markerImageView.setImageDrawable(resources.getDrawable(R.drawable.img_placeholder))
            changeMarker(marker, customMarkerView)
        }

        lastClickedMarker = marker
    }

    /**
     *
     * Expand target marker
     */

    private fun changeMarker(marker : Marker, customView : View) {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(customView)))
        marker.zIndex = 1.0f
        refreshMap()
    }

    private fun setBackgroundForCustomMarker(backgroundView : View, problemId : Int) {
        backgroundView.background = when (problemId) {
            1 -> resources.getDrawable(R.drawable.custom_marker_background_green)
            2 -> resources.getDrawable(R.drawable.custom_marker_background_brown)
            3 -> resources.getDrawable(R.drawable.custom_marker_background_blue)
            4 -> resources.getDrawable(R.drawable.custom_marker_background_yellow)
            5 -> resources.getDrawable(R.drawable.custom_marker_background_pink)
            6 -> resources.getDrawable(R.drawable.custom_marker_background_red)
            else -> resources.getDrawable(R.drawable.custom_marker_background_grey)
        }
    }

    fun refreshMap(nullifyLastClickedMarker : Boolean = false, clearMarkers : Boolean = false) {
        if (this::progressBar.isInitialized) {
            progressBar.visibility = View.GONE
            progressBar.isIndeterminate = false
        }

        if (this::googleMap.isInitialized && clearMarkers) {
            googleMap.clear()
        }

        if (nullifyLastClickedMarker) {
            lastClickedMarker = null
        }
    }

    private fun loadImage(url : String, size : Int, targetView : ImageView, callback: Callback? = null) {
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.img_placeholder)
                .resize(size, size)
                .centerCrop()
                .noFade()
                .into(targetView, callback)
    }

    private fun dpToPx(dp : Int) : Float {
        val density : Float = this.resources.displayMetrics.density
        return dp * density
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnApplicationMapFragmentInteractionListener {

        fun onInitCurrentLocation(googleMap: GoogleMap)

        fun onRequestingMarkers(googleMap: GoogleMap)

        fun onRequestMarkerInfo(position: LatLng) : Discussions?
    }


}
