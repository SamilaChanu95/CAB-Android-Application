 package com.example.cab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button CustomerLoginButton ;
    private Button CustomerRegisterButton ;
    private TextView CustomerRegisterLink;
    private TextView CustomerStatus;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;
    private ProgressDialog LoadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mAuth = FirebaseAuth.getInstance();

        CustomerLoginButton = (Button) findViewById(R.id.customer_login_btn);
        CustomerRegisterButton = (Button) findViewById(R.id.customer_register_btn);
        CustomerRegisterLink = (TextView) findViewById(R.id.register_customer_link);
        CustomerStatus = (TextView) findViewById(R.id.customer_status);
        EmailCustomer = (EditText) findViewById(R.id.email_customer);
        PasswordCustomer = (EditText) findViewById(R.id.password_customer);
        LoadingBar = new ProgressDialog(this);

        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);

        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);

            }
        });

        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                RegisterCustomer(email, password);

            }
        });

        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                SignInCustomer(email, password);

            }
        });
    }

     public static boolean isEmailValid(String email) {
         final String EMAIL_PATTERN =
                 "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9]+(\\.[a-z0-9]+)*(\\.[a-z]{3})$";
         final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
         final Matcher matcher = pattern.matcher(email);
         return matcher.matches();
     }

     public boolean isValidPassword(final String password) {

         Pattern pattern;
         Matcher matcher;

         final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*@#$%^&+=])(?=\\S+$).{4,}$";

         pattern = Pattern.compile(PASSWORD_PATTERN);
         matcher = pattern.matcher(password);

         return matcher.matches();

     }

     private void RegisterCustomer(String email, String password) {

         if(TextUtils.isEmpty(email)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your e-mail address...", Toast.LENGTH_SHORT).show();
         }
         if(TextUtils.isEmpty(password)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your password...", Toast.LENGTH_SHORT).show();
         }
         if(!isEmailValid(email)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter valid e-mail address...", Toast.LENGTH_SHORT).show();
         }
         if(!isValidPassword(password)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter at least one number, symbol, uppercase letter and lowercase letter into password...", Toast.LENGTH_SHORT).show();
         }
         else {
             LoadingBar.setTitle("Customer Registration");
             LoadingBar.setMessage("Please wait, while we are register your data...");
             LoadingBar.show();

             mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if(task.isSuccessful()) {
                         Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Register successfully..", Toast.LENGTH_SHORT).show();
                         LoadingBar.dismiss();
                     }
                     else {

                         Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Register not successfully..", Toast.LENGTH_SHORT).show();
                         LoadingBar.dismiss();
                     }
                 }
             });
         }
     }

     private void SignInCustomer(String email, String password) {

         if(TextUtils.isEmpty(email)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your e-mail address...", Toast.LENGTH_SHORT).show();
         }
         if(TextUtils.isEmpty(password)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your password...", Toast.LENGTH_SHORT).show();
         }
         if(!isEmailValid(email)) {
             Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter valid e-mail address...", Toast.LENGTH_SHORT).show();
         }
         else {
             LoadingBar.setTitle("Customer SignIn");
             LoadingBar.setMessage("Please wait, while we are checking your credientials...");
             LoadingBar.show();

             mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if(task.isSuccessful()) {
                         Toast.makeText(CustomerLoginRegisterActivity.this, "Customer SignIn successfully..", Toast.LENGTH_SHORT).show();
                         LoadingBar.dismiss();
                     }
                     else {

                         Toast.makeText(CustomerLoginRegisterActivity.this, "Customer SignIn not successfully..", Toast.LENGTH_SHORT).show();
                         LoadingBar.dismiss();
                     }
                 }
             });
         }
     }
 }


