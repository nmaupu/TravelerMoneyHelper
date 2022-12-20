package org.maupu.android.tmh.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.maupu.android.tmh.R;

public class GDriveBackupPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    public static final Class<GDriveBackupPreferenceDialogFragmentCompat> TAG = GDriveBackupPreferenceDialogFragmentCompat.class;

    private ActivityResultLauncher<Intent> googleSignInStartForResult;

    private SignInButton signInButton;
    private Button signOutButton;

    private LinearLayout layoutInfoLogin;
    private TextView textViewEmail;

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount googleSignInAccount;

    public static GDriveBackupPreferenceDialogFragmentCompat newInstance(String key) {
        final GDriveBackupPreferenceDialogFragmentCompat fragment = new GDriveBackupPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireActivity());

        googleSignInStartForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                });
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        signOutButton = view.findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(v -> {
            if (v.getId() == R.id.sign_out_button) {
                googleSignInClient.signOut().addOnCompleteListener(command -> displaySignInLayout());
            }
        });

        signInButton = view.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> {
            if (v.getId() == R.id.sign_in_button) {
                launchSignInIntent();
            }
        });

        layoutInfoLogin = view.findViewById(R.id.layout_info_login);
        textViewEmail = view.findViewById(R.id.text_view_email);
        if (googleSignInAccount != null) {
            displayLoggedInLayout();
        } else {
            displaySignInLayout();
        }
    }

    private void displaySignInLayout() {
        layoutInfoLogin.setVisibility(View.GONE);
        signInButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.GONE);
    }

    private void displayLoggedInLayout() {
        layoutInfoLogin.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
        textViewEmail.setText(googleSignInAccount.getEmail());
    }

    private void launchSignInIntent() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInStartForResult.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);
            if (googleSignInAccount != null) {
                // Signed in successfully, show authenticated UI.
                displayLoggedInLayout();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG.getName(), "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
