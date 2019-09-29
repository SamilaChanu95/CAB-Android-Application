package com.example.cab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.ContactsContract;
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

public class DriverLoginRegisterActivity extends AppCompatActivity {

    private Button DriverLoginButton ;
    private Button DriverRegisterButton ;
    private TextView DriverRegisterLink;
    private TextView DriverStatus;
    private EditText EmailDriver;
    private EditText PasswordDriver;
    private ProgressDialog LoadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        mAuth = FirebaseAuth.getInstance();

        DriverLoginButton = (Button) findViewById(R.id.driver_login_btn);
        DriverRegisterButton = (Button) findViewById(R.id.driver_register_btn);
        DriverRegisterLink = (TextView) findViewById(R.id.register_driver_link);
        DriverStatus = (TextView) findViewById(R.id.driver_status);
        EmailDriver = (EditText) findViewById(R.id.email_driver);
        PasswordDriver = (EditText) findViewById(R.id.password_driver);
        LoadingBar = new ProgressDialog(this);

        DriverRegisterButton.setVisibility(View.INVISIBLE);
        DriverRegisterButton.setEnabled(false);

        DriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DriverLoginButton.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Register Driver");

                DriverRegisterButton.setVisibility(View.VISIBLE);
                DriverRegisterButton.setEnabled(true);

            }
        });

        DriverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailDriver.getText().toString();
                String password = PasswordDriver.getText().toString();

                RegisterDriver(email, password);

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

    private void RegisterDriver(String email, String password) {

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter your e-mail address...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter your password...", Toast.LENGTH_SHORT).show();
        }
        if(!isEmailValid(email)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter valid e-mail address...", Toast.LENGTH_SHORT).show();
        }
        if(!isValidPassword(password)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter at least one number, symbol, uppercase letter and lowercase letter into password...", Toast.LENGTH_SHORT).show();
        }
        else {
                LoadingBar.setTitle("Driver Registration");
                LoadingBar.setMessage("Please wait, while we are register your data...");
                LoadingBar.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(DriverLoginRegisterActivity.this, "Driver Register successfully..", Toast.LENGTH_SHORT).show();
                            LoadingBar.dismiss();
                        }
                        else {

                            Toast.makeText(DriverLoginRegisterActivity.this, "Driver Register not successfully..", Toast.LENGTH_SHORT).show();
                            LoadingBar.dismiss();
                        }
                    }
                });
        }
    }
}
