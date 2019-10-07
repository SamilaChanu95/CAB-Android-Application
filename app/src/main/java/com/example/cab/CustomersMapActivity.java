package com.example.cab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telecom.DisconnectCause;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;

    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button CustomerLogoutButton;
    private Button CallCabCarButton;
    private Button CustomerSettingsButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    Marker DriverMarker, PickUpMarker;
    GeoQuery geoQuery;
    private DatabaseReference CustomerDatabaseRef;
    private DatabaseReference DriverAvailableRef;
    private DatabaseReference DriversRef;
    private DatabaseReference DriverLocationRef;

    private String customerID;
    private LatLng CustomerPickUpLocation;
    private int radius = 1;
    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;

    private ValueEventListener DriverLocationRefListener;

    private TextView txtName, txtPhone, txtCarName;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        CustomerLogoutButton = (Button) findViewById(R.id.customer_logout_btn);
        CallCabCarButton = (Button) findViewById(R.id.customer_call_btn);
        CustomerSettingsButton = (Button) findViewById(R.id.customer_settings_btn);

        txtName = (TextView) findViewById(R.id.name_driver);
        txtPhone = (TextView) findViewById(R.id.phone_number_driver);
        txtCarName = (TextView) findViewById(R.id.car_name_driver);

        profilePic = (CircleImageView) findViewById(R.id.profile_image_driver);

        relativeLayout = (RelativeLayout) findViewById(R.id.rel1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        CustomerSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent SettingsIntent = new Intent(CustomersMapActivity.this, SettingsActivity.class);
                SettingsIntent.putExtra("type","Customers");
                startActivity(SettingsIntent);

            }
        });

        CustomerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.signOut();
                LogoutCustomer();

            }
        });

        CallCabCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(requestType) {

                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListener);

                    if(driverFound != null ) {

                        DriversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");
                        DriversRef.removeValue();
                        driverFoundID = null;

                    }

                    driverFound = false;
                    radius = 1;
                    customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.removeLocation(customerID);

                    if(PickUpMarker != null) {

                        PickUpMarker.remove();

                    }
                    if(DriverMarker != null) {

                        DriverMarker.remove();

                    }

                    CallCabCarButton.setText("Call a Cab");

                    relativeLayout.setVisibility(View.GONE);

                }
                else {

                    requestType = true;
                    customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    CustomerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));


                    CallCabCarButton.setText("Getting your Driver..");// when click the call button then show that message

                    //get the closest driver accourding to the location of the customer
                    GetClosestDriverCab();

                }
            }
        });
    }

    private void GettingDriverLocation() {

        DriverLocationRefListener = DriverLocationRef.child(driverFoundID).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestType) {

                    List<Object> driverLocationMap  = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    CallCabCarButton.setText("Drivers Found..");

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedDriverInformation();

                    if(driverLocationMap.get(0) != null) {

                        LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());//convert the double data type value

                    }
                    if(driverLocationMap.get(1) != null) {

                        LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());

                    }

                    //adding marker - to pointing where driver is - using this lat lng
                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    if(DriverMarker != null) {

                        DriverMarker.remove();

                    }

                    //get the customer location by using lng abd lat
                    Location location1  = new Location("");
                    location1.setLatitude(CustomerPickUpLocation.latitude);
                    location1.setLongitude(CustomerPickUpLocation.longitude);


                    //get the driver location by using lng abd lat
                    Location location2  = new Location("");
                    location2.setLatitude(DriverLatLng.latitude);
                    location2.setLongitude(DriverLatLng.longitude);

                    //Display the distance
                    float Distance = location1.distanceTo(location2);

                    if(Distance < 90)
                    {
                        CallCabCarButton.setText("Driver's Reached");
                    }
                    else
                    {
                        CallCabCarButton.setText("Driver found: "+ String.valueOf(Distance));
                    }

                    DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("your driver is here.").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //after 1s again update the location
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void LogoutCustomer()
    {
        Intent welcomeIntent = new Intent(CustomersMapActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }

    private void GetClosestDriverCab()
    {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude, CustomerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!driverFound  && requestType)
                {
                    driverFound = true;
                    driverFoundID = key;

                    //create referene for get the data from firebase database
                    DriversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", customerID);//who need the cars get the customers IDs
                    DriversRef.updateChildren(driverMap);

                    GettingDriverLocation();
                    CallCabCarButton.setText("looking for driver locations...");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!driverFound)
                {
                    radius = radius + 1;
                    GetClosestDriverCab();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getAssignedDriverInformation() {

        //get the data into reference variable by firebase database with driverFoundID value
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String car = dataSnapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarName.setText(car);

                    if(dataSnapshot.hasChild("image"))
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
