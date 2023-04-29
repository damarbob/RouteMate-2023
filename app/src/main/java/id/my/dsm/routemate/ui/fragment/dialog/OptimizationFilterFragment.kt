package id.my.dsm.routemate.ui.fragment.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import id.my.dsm.routemate.R

class OptimizationFilterFragment : DialogFragment() {

    // Use this instance of the interface to deliver action events
    private lateinit var listener: OptimizationFilterListener

    private lateinit var autocompleteOptimizationVehiclesList: AutoCompleteTextView
    private lateinit var autocompleteOptimizationTripsList: AutoCompleteTextView

    private lateinit var tripNames: java.util.ArrayList<String>
    private lateinit var vehicleNames: java.util.ArrayList<String>

    private var filterMap: MutableMap<String, Int> = mapOf(
            Pair("vehicleIndex", -1),
            Pair("tripIndex", -1),
    ) as MutableMap<String, Int>

    var caller: Fragment? = null

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface OptimizationFilterListener {
        fun onApplyOptimizationFilter(filterMap: Map<String, Int>)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_optimization_filter, container, false)

        autocompleteOptimizationVehiclesList = view.findViewById(R.id.autocompleteOptimizationVehiclesList)
        autocompleteOptimizationTripsList = view.findViewById(R.id.autocompleteOptimizationTripsList)

        tripNames = arguments?.getStringArrayList("tripNames")!!
        vehicleNames = arguments?.getStringArrayList("vehicleNames")!!

        val btnApply = view.findViewById<MaterialButton>(R.id.buttonOptimizationFilterAppy)

        autocompleteOptimizationVehiclesList.setOnItemClickListener { _, _, i, _ ->
            // Use add in future when the filters are mixable. Don't forget to check whether or not the tag exists in the list.
            // For now, just replace the first item whenever user selects the item in the filter dropdown
            filterMap["vehicleIndex"] = i
            filterMap["tripIndex"] = -1
        }
        autocompleteOptimizationTripsList.setOnItemClickListener { _, _, i, _ ->
            // Use add in future when the filters are mixable. Don't forget to check whether or not the tag exists in the list.
            // For now, just replace the first item whenever user selects the item in the filter dropdown
            filterMap["vehicleIndex"] = -1
            filterMap["tripIndex"] = i
        }
        btnApply.setOnClickListener {
            listener.onApplyOptimizationFilter(filterMap)
            dismiss()
        }

        return view

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (caller == null)
            throw NullPointerException("Caller cannot be null!")

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = caller as OptimizationFilterListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((caller.toString() +
                    " must implement OptimizationFilterListener"))
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onResume() {
        super.onResume()

        val adapterAutocompleteRoutesList: ArrayAdapter<String> = ArrayAdapter<String>(context!!, R.layout.list_item_basic, tripNames)
        adapterAutocompleteRoutesList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        autocompleteOptimizationTripsList.setAdapter(adapterAutocompleteRoutesList)

        val adapterAutocomplete: ArrayAdapter<String> = ArrayAdapter<String>(context!!, R.layout.list_item_basic, vehicleNames)
        adapterAutocomplete.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        autocompleteOptimizationVehiclesList.setAdapter(adapterAutocomplete)

    }
}