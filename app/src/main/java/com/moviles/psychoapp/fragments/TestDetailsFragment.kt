package com.moviles.psychoapp.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.moviles.psychoapp.R
import com.moviles.psychoapp.world.Test
import kotlinx.android.synthetic.main.fragment_test_details.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TestDetailsFragment.OnTestDetailsFragmentListener] interface
 * to handle interaction events.
 * Use the [TestDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestDetailsFragment : Fragment() {

    private var testId: String? = null
    private var testTime: Long? = null
    private var testWeight: Float? = null
    private var testMaxValue: Long? = null
    private var mListener: OnTestDetailsFragmentListener? = null
    private lateinit var test: Test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            testId = arguments.getString(ARG_TEST_ID)
            testTime = arguments.getLong(ARG_TEST_TIME)
            testWeight = arguments.getFloat(ARG_TEST_WEIGHT)
            testMaxValue = arguments.getLong(ARG_TEST_MAXVALUE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_test_details, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_test_details_btn_start.isEnabled = false
        FirebaseDatabase.getInstance().getReference("tests").child(testId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(ds: DatabaseError) {}
                    override fun onDataChange(ds: DataSnapshot) {
                        if (ds.exists()) {
                            test = ds.getValue(Test::class.java)!!
                            f_test_details_btn_start.isEnabled = true
                            f_test_details_title.text = test.name
                            f_test_details_description.text = test.description
                            f_test_details_instructions.text = test.instructions
                            f_test_details_time.text = testTime.toString()
                            f_test_details_max_score.text = testMaxValue.toString()
                            f_test_details_max_weight.text = testWeight.toString()
                        }
                    }
                })
        f_test_details_btn_start.setOnClickListener { onButtonPressed(test.privateId) }
    }

    fun onButtonPressed(action: String) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(action)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTestDetailsFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTestDetailsFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnTestDetailsFragmentListener {
        fun onFragmentInteraction(action: String)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_TEST_ID = "testId"
        private val ARG_TEST_TIME = "testTime"
        private val ARG_TEST_WEIGHT = "testWeight"
        private val ARG_TEST_MAXVALUE = "testMaxValue"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param testId Parameter 1.
         * @return A new instance of fragment TestDetailsFragment.
         */
        fun newInstance(testId: String, testTime: Long, testWeight: Float, testMaxValue: Long): TestDetailsFragment {
            val fragment = TestDetailsFragment()
            val args = Bundle()
            args.putString(ARG_TEST_ID, testId)
            args.putLong(ARG_TEST_TIME, testTime)
            args.putFloat(ARG_TEST_WEIGHT, testWeight)
            args.putLong(ARG_TEST_MAXVALUE, testMaxValue)
            fragment.arguments = args
            return fragment
        }
    }
}
