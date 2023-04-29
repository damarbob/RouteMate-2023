package id.my.dsm.routemate.ui.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnUserStatusChangedEvent;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.databinding.FragmentLoginBinding;
import id.my.dsm.routemate.ui.fragment.viewmodel.LoginViewModel;
import id.my.dsm.routemate.usecase.user.GetDisplayNameUseCase;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    // Dependencies
    @Inject
    UserRepository userRepository;
    private LoginViewModel mViewModel;

    // Use case
    @Inject
    GetDisplayNameUseCase getDisplayNameUseCase;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class); // Provide ViewModel

        binding.textInfo.setMovementMethod(LinkMovementMethod.getInstance());

        // Set listeners
        binding.buttonLogin.setOnClickListener(v -> {

            binding.buttonLogin.setEnabled(false); // Disable button login to prevent multiple requests

            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Field cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            userRepository.signIn(email, password); // Sign user in

        });
        binding.buttonSwitch.setOnClickListener(v -> {
            // Switch to RegisterFragment
            NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment);
        });
        binding.buttonBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack
        });

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

        binding.buttonLogin.setEnabled(true); // Enable button login again

        // Handles user sign in status
        switch(event.getUserStatus()) {
            case SIGN_IN_SUCCESS:
                Toast.makeText(requireContext(), "Welcome back, " + getDisplayNameUseCase.invoke(), Toast.LENGTH_SHORT).show();

                // Switch to HomeFragment
                NavHostFragment.findNavController(this).navigate(R.id.action_global_homeFragment);
                break;
            case SIGN_IN_FAILURE:
                Toast.makeText(requireContext(), "Failed to sign in: " + event.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}