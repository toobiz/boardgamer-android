package pl.tubis.boardgamer.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import pl.tubis.boardgamer.Activities.MainActivity;
import pl.tubis.boardgamer.Model.Game;
import pl.tubis.boardgamer.Model.NewMarker;
import pl.tubis.boardgamer.Model.User;
import pl.tubis.boardgamer.R;

import static pl.tubis.boardgamer.Activities.MainActivity.myUid;

/**
 * Created by mike on 05.12.2016.
 */

public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    Map<String, String> mMarkers = new HashMap<String, String>();
    static final Integer LOCATION = 0x1;
    GoogleApiClient client;
    LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;
//    LatLng myPosition;
    private Button checkinButton;
    SimpleLocation myLocation;
    Double myLatitude;
    Double myLongitude;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        myLocation = new SimpleLocation(getActivity().getApplicationContext());
        myLatitude = myLocation.getLatitude();
        myLongitude = myLocation.getLongitude();

        if (!myLocation.hasLocationEnabled() || myLocation.getLongitude() == 0) {
            // ask the user to enable location access
            SimpleLocation.openSettings(getActivity());
        }

        client = new GoogleApiClient.Builder(getActivity())
//                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
            addMarkers(true);

            }
        });

        checkinButton = (Button) rootView.findViewById(R.id.checkin_button);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkinButton.getText() == "Check out") {
                    googleMap.clear();
                    mMarkers.clear();
                    ((MainActivity)getActivity()).locationsRef.child(myUid).setValue(null);
                    checkinButton.setText("Check in");
                } else {
                    Toast.makeText(getActivity(), myLatitude + " " + myLongitude , Toast.LENGTH_LONG).show();
                    googleMap.clear();
                    mMarkers.clear();
                    addMarkers(false);
                    NewMarker newMarker = new NewMarker(myLatitude, myLongitude, myUid);
                    ((MainActivity)getActivity()).locationsRef.child(myUid).setValue(newMarker);
                    checkinButton.setText("Check out");
                }

            }
        });


        return rootView;
    }

    private void addMarkers(Boolean zoom){
    String mUid;
        downloadLocations(googleMap);
//                askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION, mMap);

//                // For adding a marker at a point on the Map
        final double latitude = myLocation.getLatitude();
        final double longitude = myLocation.getLongitude();
        LatLng myPosition = new LatLng(latitude, longitude);
//                                mMap.addMarker(new MarkerOptions().position(myPosition).title("It's Me!"));
//                mMap.addMarker(addNewMarker(myPosition));
////
////            ////                 For zooming to the location of the user

        if (zoom == true){
            CameraPosition cameraPosition = new CameraPosition.Builder().target(myPosition).zoom(10).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                marker.setTitle("Loading...");
                marker.setSnippet(" ");
                marker.showInfoWindow();
                final String uid = mMarkers.get(marker.getId());

                if (uid != null){
                    downloadUserData(marker, uid);
                }

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {
                        Fragment fragment = new UserProfileFragment();
                        Bundle args = new Bundle();
                        args.putString("uid", uid);
                        fragment.setArguments(args);
                        ((MainActivity)getActivity()).createFragment(fragment);
                    }
                });

                return true;
            }
        });

    }

//    @Override
//    public void onProviderEnabled(String provider) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        // TODO Auto-generated method stub
//
//        double latitude = (double) (location.getLatitude());
//        double longitude = (double) (location.getLongitude());
//
//        Log.i("Geo_Location", "Latitude: " + latitude + ", Longitude: " + longitude);
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        // TODO Auto-generated method stub
//
//    }

//    private void askForPermission(String permission, Integer requestCode, GoogleMap mMap) {
//
//        // Check if we're running on Android 5.0 or higher
//        if (Build.VERSION.SDK_INT >= 23) {
//
//            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
//
//                // Should we show an explanation?
//                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
//
//                    //This is called if user has denied the permission before
//                    //In this case I am just asking the permission again
//                    ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
//
//                } else {
//
//                    ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
//                }
//            } else {
//
////            showMyLocation(mMap);
////                mMap.setMyLocationEnabled(true);
////
////                // Getting LocationManager object from System Service LOCATION_SERVICE
////                LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
////
////                // Creating a criteria object to retrieve provider
////                Criteria criteria = new Criteria();
////
////                // Getting the name of the best provider
////                String provider = locationManager.getBestProvider(criteria, true);
////
////                // Getting Current Location
////                Location location = locationManager.getLastKnownLocation(provider);
//            }
//
//        } else {
////            showMyLocation(mMap);
//
//            //            if (client == null) {
////                buildGoogleApiClient();
////            mMap.setMyLocationEnabled(true);
//
////            // Getting LocationManager object from System Service LOCATION_SERVICE
////            LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
////
////            // Creating a criteria object to retrieve provider
////            Criteria criteria = new Criteria();
////
////            // Getting the name of the best provider
////            String provider = locationManager.getBestProvider(criteria, true);
////
////            // Getting Current Location
////            Location location = locationManager.getLastKnownLocation(provider);
//
////            if (location != null) {
////                // Getting latitude of the current location
////                double latitude = location.getLatitude();
////
////                // Getting longitude of the current location
////                double longitude = location.getLongitude();
////
////                // Creating a LatLng object for the current location
////                LatLng latLng = new LatLng(latitude, longitude);
////
////                myPosition = new LatLng(latitude, longitude);
////
////                mMap.addMarker(new MarkerOptions().position(myPosition).title("It's Me!"));
////
////                ////                 For zooming to the location of the user
////                CameraPosition cameraPosition = new CameraPosition.Builder().target(myPosition).zoom(10).build();
////                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
////            }
//        }
//
//
//    }

//    private void showMyLocation(GoogleMap mMap){
//        //            Toast.makeText(getActivity(), "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
////            if (client == null) {
////                buildGoogleApiClient();
//        mMap.setMyLocationEnabled(true);
//
//        // Getting LocationManager object from System Service LOCATION_SERVICE
//        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
//
//        // Creating a criteria object to retrieve provider
//        Criteria criteria = new Criteria();
//
//        // Getting the name of the best provider
//        String provider = locationManager.getBestProvider(criteria, true);
//
//        // Getting Current Location
//        Location location = locationManager.getLastKnownLocation(provider);
//
//        if (location != null) {
//            // Getting latitude of the current location
//            double latitude = location.getLatitude();
//
//            // Getting longitude of the current location
//            double longitude = location.getLongitude();
//
//            // Creating a LatLng object for the current location
//            LatLng latLng = new LatLng(latitude, longitude);
//
//            myPosition = new LatLng(latitude, longitude);
//
//            mMap.addMarker(new MarkerOptions().position(myPosition).title("It's Me!"));
//
//            ////                 For zooming to the location of the user
//            CameraPosition cameraPosition = new CameraPosition.Builder().target(myPosition).zoom(10).build();
//            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }
//    }

    private MarkerOptions addNewMarker(LatLng location){
//        LatLng warsaw = new LatLng(52, 21);
        MarkerOptions marker = new MarkerOptions().position(location).icon
                (BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker));
        return marker;
    }

    private void downloadLocations(final GoogleMap mMap){
        ((MainActivity)getActivity()).locationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    final NewMarker newMarker = postSnapshot.getValue(NewMarker.class);
                    LatLng location = new LatLng(newMarker.getLatitude(), newMarker.getLongitude());

                    com.google.android.gms.maps.model.Marker mkr = mMap.addMarker(addNewMarker(location));
                    mMarkers.put(mkr.getId(), newMarker.getUid());

                    Log.d("myTag", newMarker.getLatitude().toString());
//                    hideProgressDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    private void downloadUserData(final com.google.android.gms.maps.model.Marker marker, final String uid){
        ((MainActivity)getActivity()).usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    User person = snapshot.getValue(User.class);


                    marker.setTitle(person.getfirstName());
                    marker.setSnippet(person.getAbout());
                    marker.showInfoWindow();

//                    //Adding it to a string
//                    String string = "\nName: " + person.getfirstName() + "\n\nAddress: " + person.getEmail() + "\n\nUID: " + person.getUid() +
//                            "\n\nAbout me: " + person.getAbout() + "\n\nOneSignalID: " + person.getOneSignalID() + "\n\nPrivacy Agreement: "
//                            + person.getPrivacyAgreement() + "\n";


//                    hideProgressDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

//    private android.location.LocationListener ll = new android.location.LocationListener(){
//        public void onLocationChanged(Location location) {
//            // Getting latitude of the current location
//            double latitude = location.getLatitude();
//
//            // Getting longitude of the current location
//            double longitude = location.getLongitude();
//
//            // Creating a LatLng object for the current location
//            LatLng latLng = new LatLng(latitude, longitude);
//
//            myPosition = new LatLng(latitude, longitude);
//
//
//
//            googleMap.addMarker(new MarkerOptions().position(myPosition).title("It's Me!"));
//
//            ////                 For zooming to the location of the user
//            CameraPosition cameraPosition = new CameraPosition.Builder().target(myPosition).zoom(10).build();
//            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }
//        public void onProviderDisabled(String provider) {}
//        public void onProviderEnabled(String provider) {}
//        public void onStatusChanged(String provider, int status, Bundle extras) {}
//    };

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        // make the device update its location
        myLocation.beginUpdates();
    }

    @Override
    public void onPause() {
        // stop location updates (saves battery)
        myLocation.endUpdates();
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }
}