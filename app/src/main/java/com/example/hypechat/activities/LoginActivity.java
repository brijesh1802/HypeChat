package com.example.hypechat.activities;

import static com.example.hypechat.utilities.Constants.TAG;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.hypechat.utilities.EmailSender;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hypechat.R;
import com.example.hypechat.components.User;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    //private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
   // });
    EditText email, password;
    Button loginButton, googleButton, forgetPasswordButton;
    TextView signupText;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    CollectionReference usersReference;
    SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String ALERT_DIALOG_SHOWN_KEY = "alertDialogShown";
    SharedPreferences sharedPreferencs;
    private static final String PREFSS_NAME = "MyPrefs";
    int MAX_LOGIN_ATTEMPTS = 3;
    private static final String LOGIN_ATTEMPTS_KEY = "loginAttempts";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        googleButton = findViewById(R.id.btn_google);
        forgetPasswordButton = findViewById(R.id.btn_forget_password);
        signupText = findViewById(R.id.txt_signup);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sharedPreferencs = getSharedPreferences(PREFSS_NAME, MODE_PRIVATE);

        int loginAttempts = sharedPreferencs.getInt(LOGIN_ATTEMPTS_KEY, 0);
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            sendPasswordChangeEmail(email.getText().toString());
        }
        boolean alertDialogShown = sharedPreferences.getBoolean(ALERT_DIALOG_SHOWN_KEY, false);

        if (!alertDialogShown) {
            // Show the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Note:");
            builder.setMessage("If you are using Android 13 or above, please enable notifications for the app in settings to continue using the app. Ignore this message if you have already enabled notifications.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Perform any action you want when the OK button is clicked
                    dialog.dismiss(); // Dismiss the dialog

                    // Set the flag indicating that the dialog has been shown
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ALERT_DIALOG_SHOWN_KEY, true);
                    editor.apply();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        auth = FirebaseAuth.getInstance();
        usersReference = FirebaseFirestore.getInstance().collection("Users");
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this
                , googleSignInOptions);


        loginButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            login(strEmail, strPassword);
        });

        signupText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupEmailActivity.class)));
        googleButton.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, 100);
        });
        forgetPasswordButton.setOnClickListener(view -> {
            showRecoverPasswordDialog();
        });
       // new Handler().postDelayed(this::askNotificationPermission, 4000);

    }
   /* private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "askNotificationPermission: FCM SDK (and your app) can post notifications.");
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this, "askNotificationPermission: enabling notifications is cool bruh!", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }*/
    void login(String strEmail, String strPassword) {
        if (strEmail.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                resetLoginAttempts();
                if (auth.getCurrentUser().isEmailVerified()) {
                    doValidUserShit();
                } else {
                    Toast.makeText(this, "Verify your email first\nLink sent to " + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    auth.getCurrentUser().sendEmailVerification();
                }
            } else {
                //Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
                incrementLoginAttempts();
                if (getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                    sendPasswordChangeEmail(strEmail);
                }
            }
        });
    }

    void incrementLoginAttempts() {
        int loginAttempts = getLoginAttempts() + 1;
        SharedPreferences.Editor editor = sharedPreferencs.edit();
        editor.putInt(LOGIN_ATTEMPTS_KEY, loginAttempts);
        editor.apply();
    }

    int getLoginAttempts() {
        return sharedPreferencs.getInt(LOGIN_ATTEMPTS_KEY, 0);
    }

    void resetLoginAttempts() {
        SharedPreferences.Editor editor = sharedPreferencs.edit();
        editor.putInt(LOGIN_ATTEMPTS_KEY, 0);
        editor.apply();
    }

    void sendPasswordChangeEmail(String strEmail) {
        String subject = "Potential Unauthorized Access to Your Account";
        String content = "Dear " + strEmail + ",\n\n" +
                "We have detected multiple failed login attempts on your account. It appears that someone may be trying to gain unauthorized access. For your account's security, we recommend changing your password immediately.\n\n" +
                "If you did not initiate these login attempts or suspect any fraudulent activity, please contact our customer support team immediately.\n\n" +
                "Thank you for your attention to this matter. Ensuring the safety of your account is our top priority.\n\n" +
                "Best regards,\n\n" +
                "Brijesh Poojary\n" +
                "HypeChat.Org\n" +
                "hypechat.org@gmail.com";
        EmailSender.sendEmail(strEmail, subject, content);
    }


    private void doValidUserShit() {
        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + auth.getUid());

        OSDeviceState device = OneSignal.getDeviceState();
        assert device != null;
        String playerID = device.getUserId();
        userReference.update("onesignalPlayerId", playerID);

        userReference.get().addOnCompleteListener(task0 -> {
            if (task0.isSuccessful()) {
                User user = task0.getResult().toObject(User.class);
                if (user == null) {
                    user = new User(auth.getUid(), "", "", auth.getCurrentUser().getEmail(), getResources().getString(R.string.default_profile_img_url), getResources().getString(R.string.default_background_img_url));
                    user.setOnesignalPlayerId(playerID);
                    userReference.set(user).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, auth.getUid(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    startAddInfoActivity();
                } else {
                    if (user.getUsername().isEmpty()) {
                        startAddInfoActivity();
                    } else {
                        startMainActivity();
                    }
                }
            } else {
                Toast.makeText(this, task0.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);

        emailEt.setText(email.getText());
        emailEt.setMinEms(14);
        emailEt.setHint("E-mail");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(30, 20, 30, 10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", (dialog, which) -> {
            String email = emailEt.getText().toString().trim();
            if (!email.isEmpty()) beginRecovery(email);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void beginRecovery(String email) {
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            loadingBar.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Recovery email sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful()) {
                Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show();
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask
                            .getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken()
                                        , null);
                        auth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        doValidUserShit();
                                    } else {
                                        Toast.makeText(this, "Authentication Failed :" +
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                } catch (ApiException e) {
                    Log.e("ApiException", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Log.e("signInAccountTask", signInAccountTask.getException().getMessage());
                signInAccountTask.getException().printStackTrace();
            }
        }
    }

    private void startAddInfoActivity() {
        Intent intent = new Intent(LoginActivity.this, AddInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}