package com.example.app_22_testmapviewpager2.main


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.app_22_testmapviewpager2.R
import com.example.app_22_testmapviewpager2.architecture.DiscussionsEntity
import com.google.android.gms.maps.GoogleMap

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_list, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun initRecycler() {
        val discussions = listener?.onRequestCurrentDiscussions()
        if (discussions != null) {
            val adapter = RecyclerAdapter(discussions, context)
            val recycler: RecyclerView? = view?.findViewById(R.id.list_fragment_recycler)
            recycler?.adapter = adapter
            recycler?.layoutManager = LinearLayoutManager(view?.context)
            recycler?.setHasFixedSize(true)
            recycler?.setItemViewCacheSize(20)
        }
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
    interface OnListFragmentInteractionListener {

        fun onRequestCurrentDiscussions() : DiscussionsEntity?
    }

}
