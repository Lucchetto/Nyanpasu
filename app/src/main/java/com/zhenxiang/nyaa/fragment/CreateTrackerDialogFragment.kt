package com.zhenxiang.nyaa.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.zhenxiang.nyaa.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val USERNAME = "username"
private const val SEARCH_QUERY = "searchQuery"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateTrackerDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateTrackerDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var username: String? = null
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.Theme_Nyaasi_CreateTrackerDialog)

        arguments?.let {
            username = it.getString(USERNAME)
            searchQuery = it.getString(SEARCH_QUERY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_tracker_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param username Parameter 1.
         * @param searchQuery Parameter 2.
         * @return A new instance of fragment CreateTrackerDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(username: String?, searchQuery: String?) =
            CreateTrackerDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(USERNAME, username)
                    putString(SEARCH_QUERY, searchQuery)
                }
            }
    }
}