package com.example.cab;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    private Button DriverLoginButton ;
    private Button DriverRegisterButton ;
    private TextView DriverRegisterLink;
    private TextView DriverStatus;
    private EditText EmailDriver;
    private EditText PasswordDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        DriverLoginButton = (Button) findViewById(R.id.driver_login_btn);
        DriverRegisterButton = (Button) findViewById(R.id.driver_register_btn);
        DriverRegisterLink = (TextView) findViewById(R.id.register_driver_link);
        DriverStatus = (TextView) findViewById(R.id.driver_status);
        EmailDriver = (EditText) findViewById(R.id.email_driver);
        PasswordDriver = (EditText) findViewById(R.id.password_driver);

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



}
