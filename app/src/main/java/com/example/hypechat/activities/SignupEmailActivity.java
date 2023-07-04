package com.example.hypechat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hypechat.utilities.EmailSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hypechat.R;

import java.util.Objects;

public class SignupEmailActivity extends AppCompatActivity {
    EditText email, password, confirmPassword;
    Button signupButton;
    FirebaseFirestore db;
    FirebaseAuth auth;
    ProgressDialog pd;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_email);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        signupButton = findViewById(R.id.btn_signup);

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        signupButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            String strConfirmPassword = confirmPassword.getText().toString();
            if (validate(strEmail, strPassword, strConfirmPassword)) {
                signupWithEmail(strEmail, strPassword);
            } else {
                Toast.makeText(this, "Enter all details properly", Toast.LENGTH_SHORT).show();
            }
        });
    }


   // private boolean validate(String strEmail, String strPassword, String strConfirmPassword) {
        //return !strEmail.isEmpty() && !strPassword.isEmpty() && strPassword.length() >= 6 && strPassword.equals(strConfirmPassword) &&  strPassword.length()<=15 ;
        private boolean validate(String strEmail, String strPassword, String strConfirmPassword) {
            boolean isValid = true;

            if (strEmail.isEmpty()) {
                email.setError("Enter email");
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                email.setError("Invalid email format");
                isValid = false;
            }

            if (strPassword.isEmpty()) {
                password.setError("Enter password");
                isValid = false;
            } else if (strPassword.length() < 6 || strPassword.length() > 15) {
                password.setError("Password length should be between 6 and 15 characters");
                isValid = false;
            } else if (!strPassword.matches(".*[a-zA-Z].*")) {
                password.setError("Password must contain at least one letter");
                isValid = false;
            } else if (!strPassword.matches(".*\\d.*")) {
                password.setError("Password must contain at least one digit");
                isValid = false;
            } else if (!strPassword.matches(".*[^a-zA-Z0-9].*")) {
                password.setError("Password must contain at least one special character");
                isValid = false;
            }

            if (!strPassword.equals(strConfirmPassword)) {
                password.setError("Password and Confirm Password must be the same");
                confirmPassword.setError("Password and Confirm Password must be the same");
                isValid = false;
            }

            return isValid;
        }


   /* private void signupWithEmail(String strEmail, String strPassword) {
        pd.setMessage("Please Wait");
        pd.show();
        auth.createUserWithEmailAndPassword(strEmail, strPassword).addOnSuccessListener(authResult -> {
            authResult.getUser().sendEmailVerification().addOnSuccessListener(unused -> {
                Toast.makeText(SignupEmailActivity.this, "Verification mail sent to " + authResult.getUser().getEmail(), Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }*/
   private void signupWithEmail(String strEmail, String strPassword) {
       pd.setMessage("Please Wait");
       pd.show();

       // Check if the email is already registered with another account
       auth.fetchSignInMethodsForEmail(strEmail)
               .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       boolean isNewUser = Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();
                       if (isNewUser) {
                           // Email is not registered with another account, proceed with sign up
                           auth.createUserWithEmailAndPassword(strEmail, strPassword)
                                   .addOnSuccessListener(authResult -> {
                                       Objects.requireNonNull(authResult.getUser()).sendEmailVerification()
                                               .addOnSuccessListener(unused -> {
                                                   Toast.makeText(SignupEmailActivity.this,
                                                           "Verification mail sent to " + authResult.getUser().getEmail(),
                                                           Toast.LENGTH_LONG).show();
                                                   finish();
                                               });
                                   });
                       } else {
                           // Email is already registered with another account
                           Toast.makeText(SignupEmailActivity.this, "Email is already registered with another account",
                                   Toast.LENGTH_SHORT).show();
                           pd.dismiss();
                           String subject = "Potential Unauthorized Access to Your Account";
                           String content = "<html><body>" +
                                   "<p>Dear " + strEmail + ",</p>" +
                                   "<p>We have detected suspicious activity on your account associated with the email address " + strEmail + ". It appears that someone may be attempting to access your account without authorization.</p>" +
                                   "<p>For your account's security, we strongly advise you to change your password immediately. Taking this action will help protect your personal information and prevent any unauthorized access.</p>" +
                                   "<p> Ensuring the safety of your account is our top priority.</p>" +
                                   "<p>Best regards,</p>" +
                                   "<p>Brijesh Poojary<br>HypeChat.Org<br>hypechat.org@gmail.com</p>" +
                                   "</body></html>";

                           EmailSender.sendEmail(strEmail, subject, content);
                           Intent i = new Intent(this,LoginActivity.class);
                           startActivity(i);
                       }
                   } else {
                       // Error occurred while checking for existing account
                       Toast.makeText(SignupEmailActivity.this, "Error, Please try again later", Toast.LENGTH_SHORT).show();
                   }
               });
   }

}