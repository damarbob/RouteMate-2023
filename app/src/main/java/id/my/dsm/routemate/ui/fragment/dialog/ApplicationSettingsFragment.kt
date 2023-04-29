package id.my.dsm.routemate.ui.fragment.dialog

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import id.my.dsm.routemate.R
import id.my.dsm.routemate.databinding.FragmentApplicationSettingsBinding
import id.my.dsm.routemate.ui.model.DistanceMeasurementUnit
import id.my.dsm.routemate.ui.model.RouteMatePref


class ApplicationSettingsFragment : DialogFragment() {

    lateinit var binding: FragmentApplicationSettingsBinding

    // Settings
    lateinit var distanceMeasurementUnit: DistanceMeasurementUnit
    lateinit var applicationThemeValue: String

    override fun onStart() {
        super.onStart()

        // Fullscreen
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read preferences for use in UI
        applicationThemeValue = RouteMatePref.readString(activity, RouteMatePref.APPLICATION_THEME_KEY, RouteMatePref.APPLICATION_THEME_VALUE_AUTO)
        distanceMeasurementUnit = RouteMatePref.readMeasurementDistanceUnit(activity!!)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentApplicationSettingsBinding.inflate(inflater, container, false)

        // Apply UI based on the data obtained from preferences
        when (applicationThemeValue) { // Theme setting
            RouteMatePref.APPLICATION_THEME_VALUE_AUTO -> binding.radioButtonSettingsThemeAuto.isChecked = true
            RouteMatePref.APPLICATION_THEME_VALUE_LIGHT -> binding.radioButtonSettingsThemeLight.isChecked = true
            RouteMatePref.APPLICATION_THEME_VALUE_DARK -> binding.radioButtonSettingsThemeDark.isChecked = true
        }
        binding.autocompleteSettingsMeasurementUnit.setText(distanceMeasurementUnitToString(distanceMeasurementUnit)) // Measurement unit setting

        // Listeners
        binding.radioButtonSettingsThemeAuto.setOnCheckedChangeListener { compoundButton, b ->
            // Apply theme and save settings on checked
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) // Apply theme
                RouteMatePref.saveString(activity, RouteMatePref.APPLICATION_THEME_KEY, RouteMatePref.APPLICATION_THEME_VALUE_AUTO); // Save theme
            }
        }
        binding.radioButtonSettingsThemeLight.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                RouteMatePref.saveString(activity, RouteMatePref.APPLICATION_THEME_KEY, RouteMatePref.APPLICATION_THEME_VALUE_LIGHT);
            }
        }
        binding.radioButtonSettingsThemeDark.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                RouteMatePref.saveString(activity, RouteMatePref.APPLICATION_THEME_KEY, RouteMatePref.APPLICATION_THEME_VALUE_DARK);
            }
        }

        binding.autocompleteSettingsMeasurementUnit.setOnItemClickListener { adapterView, view, i, l ->
            distanceMeasurementUnit = indexToDistanceMeasurementUnit(i)
        }
        binding.buttonSettingsBack.setOnClickListener {
            dismiss()
        }

        return binding.root

    }

    override fun onResume() {
        super.onResume()

        // AutoCompleteTextView adapter must be declared inside onResume otherwise it will not show up correctly
        val adapter = ArrayAdapter.createFromResource(
            context!!,
            R.array.measurement_distance_unit, R.layout.list_item_basic
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.autocompleteSettingsMeasurementUnit.setAdapter(adapter)

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        RouteMatePref.saveString(activity!!, RouteMatePref.MEASUREMENT_DISTANCE_UNIT, distanceMeasurementUnit.name) // Save distance measurement unit setting

    }

    private fun indexToDistanceMeasurementUnit(int: Int): DistanceMeasurementUnit {
        return when (int) {
            0 -> DistanceMeasurementUnit.KILOMETER
            1 -> DistanceMeasurementUnit.MILE
            2 -> DistanceMeasurementUnit.METER
            else -> {DistanceMeasurementUnit.KILOMETER}
        }
    }

    private fun distanceMeasurementUnitToString(distanceMeasurementUnit: DistanceMeasurementUnit): String {
        return when (distanceMeasurementUnit) {
            DistanceMeasurementUnit.METER -> getString(R.string.settings_measurement_meter)
            DistanceMeasurementUnit.KILOMETER -> getString(R.string.settings_measurement_kilometer)
            DistanceMeasurementUnit.MILE -> getString(R.string.settings_measurement_mile)
        }
    }

    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

}