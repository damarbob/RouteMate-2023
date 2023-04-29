package id.my.dsm.routemate.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import com.google.android.material.button.MaterialButton
import id.my.dsm.routemate.R
import id.my.dsm.routemate.databinding.FragmentQuickHelpBinding

class QuickHelpFragment : DialogFragment() {

    lateinit var binding: FragmentQuickHelpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentQuickHelpBinding.inflate(inflater, container, false)

        binding.buttonQuickHelpUnderstood.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val index = arguments!!.getString("index")

        if (index.equals("adding_source"))
            Navigation.findNavController(binding.quickHelpFragmentContainer).navigate(R.id.action_global_quickHelpAddingSourceFragment)
        else if (index.equals("adding_places"))
            Navigation.findNavController(binding.quickHelpFragmentContainer).navigate(R.id.action_global_quickHelpAddingPlacesFragment)
    }
}