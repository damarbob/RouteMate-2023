package id.my.dsm.routemate.ui.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnUserStatusChangedEvent;
import id.my.dsm.routemate.ui.fragment.viewmodel.RegisterViewModel;
import id.my.dsm.routemate.usecase.user.GetDisplayNameUseCase;
import id.my.dsm.routemate.databinding.FragmentRegisterBinding;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private FragmentRegisterBinding binding;

    // Dependencies
    private RegisterViewModel mViewModel;
    @Inject
    GetDisplayNameUseCase getDisplayNameUseCase;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class); // Provide ViewModel

        binding.textInfo.setMovementMethod(LinkMovementMethod.getInstance());

        // Set listeners
        binding.buttonRegister.setOnClickListener(v -> {

            binding.buttonRegister.setEnabled(false); // Disable button register to prevent multiple requests

            String fullName = binding.editTextFullName.getText().toString();
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Field cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            mViewModel.signUp(fullName, email, password); // Sign user up

        });
        binding.buttonSwitch.setOnClickListener(v -> {
            // Switch to LoginFragment
            NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment);
        });
        binding.buttonBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack
        });

        Log.d(TAG, getDisplayNameUseCase.invoke());

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this); // Register to event

    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this); // Unregister to event
    }

    // Subscribe to OnUserStatusChangedEvent event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _onUserRepositoryEvent(OnUserStatusChangedEvent event) {

        binding.buttonRegister.setEnabled(true); // Disable button login to prevent multiple requests

//        Toast.makeText(requireContext(), "User status: " + event.getUserStatus(), Toast.LENGTH_SHORT).show(); // Show log toast
        Log.e(TAG, "Sign up status: " + event.getUserStatus()); // Log user status

        switch(event.getUserStatus()) {
            case SIGNUP_FAILURE:
            case UPDATE_USER_FAILURE:
            case INSERT_RECORD_FAILURE:
                Toast.makeText(requireContext(), event.getException().getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }

        // Get current FirebaseUser
        FirebaseUser user = mViewModel.getUser();
        if (user == null)
            return;

        // Handles user sign in status
        switch(event.getUserStatus()) {
            // TODO: Handle on update failure & insert failure
            case SIGNUP_SUCCESS:
                Toast.makeText(requireContext(), "Signed up successfully", Toast.LENGTH_SHORT).show();
                break;
            case UPDATE_USER_SUCCESS:
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                break;
            case INSERT_RECORD_SUCCESS:
                // Switch to HomeFragment
                NavHostFragment.findNavController(this).navigate(R.id.action_global_homeFragment);
        }

    }

}