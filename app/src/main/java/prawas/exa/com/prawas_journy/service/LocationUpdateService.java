package prawas.exa.com.prawas_journy.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.Set;

import prawas.exa.com.prawas_journy.Common.GPSTracker;
import prawas.exa.com.prawas_journy.model.LocationRequest;

public class LocationUpdateService extends Service {

    private boolean isUploading = false;
    private boolean isUploadingAttendance = false;
    protected DatabaseReference databaseReference;
    protected StorageReference mStorageRef;
    Set<LocationRequest> mset;
    String userId;
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;

    @Override
    public void onCreate() {

        init();
        startHandler();

    }

    protected void init() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        saveUserInformation();
    }

    private void startHandler() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                handler.postDelayed(this, 2000);
                saveUserInformation();
            }
        }, 2000);
    }

    private void updateLocation() {
        LocationRequest locationRequest = new LocationRequest();
        GPSTracker gpsTracker = new GPSTracker(this);
        locationRequest.setLattitude(gpsTracker.getLatitude());
        locationRequest.setLongitude(gpsTracker.getLongitude());

    }

    private void saveUserInformation() {

        LocationRequest locationRequest = new LocationRequest();
        GPSTracker gpsTracker = new GPSTracker(this);
        locationRequest.setLattitude(gpsTracker.getLatitude());
        locationRequest.setLongitude(gpsTracker.getLongitude());


        userId = databaseReference.push().getKey();
        databaseReference.child("location_db").child(userId).setValue(locationRequest);

        try {
            if (serviceCallbacks != null) {
                serviceCallbacks.doSomething();
            }
        } catch (Throwable e) {
            e.printStackTrace();

        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        sendDataToServer();
    }


    /**
     *
     */
    private void sendDataToServer() {

        try {
            if (serviceCallbacks != null) {
                serviceCallbacks.doSomething();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }


    public class LocalBinder extends Binder {
        public LocationUpdateService getService() {
            // Return this instance of MyService so clients can call public methods
            return LocationUpdateService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }


}
