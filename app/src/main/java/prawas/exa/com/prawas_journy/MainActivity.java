package prawas.exa.com.prawas_journy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prawas.exa.com.prawas_journy.Common.CommonActivity;
import prawas.exa.com.prawas_journy.Common.FloatingActionButton;
import prawas.exa.com.prawas_journy.model.ImageRequest;
import prawas.exa.com.prawas_journy.model.LocationRequest;
import prawas.exa.com.prawas_journy.service.LocationUpdateService;
import prawas.exa.com.prawas_journy.service.ServiceCallbacks;

public class MainActivity extends CommonActivity implements View.OnClickListener,ServiceCallbacks {

    Uri fileView;
    List<Uri> imglist = new ArrayList<>();
    private int REQUEST_CAMERA = 101, PICK_IMAGE = 102;
    Uri imageUri;
    int count = 0;
    private EditText edt_place, edt_description, edt_user;
    RatingBar ratingBar;
    ImageRequest imageRequest;
    LocationUpdateService myservice;
    private boolean bound = false;
    TextView lat,longit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_info_layout);
        init();

        edt_place = (EditText) findViewById(R.id.edt_place_name);
        edt_description = (EditText) findViewById(R.id.edt_description);
        edt_user = (EditText) findViewById(R.id.edt_user_name);
        ratingBar = (RatingBar) findViewById(R.id.ratingbar);
        lat = (TextView) findViewById(R.id.lattitude);
        longit = (TextView) findViewById(R.id.longitude);
        imageRequest = new ImageRequest();
        LinearLayout yourframelayout = (LinearLayout) findViewById(R.id.btnFloat);
        FloatingActionButton fabButton = new FloatingActionButton.Builder(this, yourframelayout)
                .withDrawable(getResources().getDrawable(R.drawable.ic_plus))
                .withButtonColor(Color.parseColor("#F43F68"))
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 2, 2)
                .create();


        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform click
                bindModel();
                if (check()) {
                    //startCamera();
                    picfromGallery();
                }

            }
        });
    }

    private void bindModel() {
        imageRequest.setPlaceName(edt_place.getText().toString().trim());
        imageRequest.setDescription(edt_description.getText().toString().trim());
        imageRequest.setUserName(edt_user.getText().toString().trim());
        imageRequest.setRating("" + ratingBar.getRating());
        imageRequest.setVisiblity("TRUE");
    }

    private void saveUserInformation() {
        String userId = databaseReference.push().getKey();
        databaseReference.child("place_data").child(userId).setValue(imageRequest);
        Toast.makeText(this, "Information Saved...", Toast.LENGTH_LONG).show();

    }

    @SuppressWarnings("VisibleForTests")
    public void uploadImage() {
        // Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        StorageReference riversRef = mStorageRef.child("images/image" + Calendar.getInstance().getTime() + ".jpg");
        riversRef.putFile(imglist.get(0))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        imageRequest.setImg(downloadUrl.toString());
                        progressDialog.dismiss();
                        Log.e("Result", "");
                        imglist.remove(0);
                        count = imglist.size();
                        if (count > 0) {
                            uploadImage();
                        } else {
                            saveUserInformation();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        progressDialog.dismiss();
                        showToast(exception.getMessage());
                        Log.e("Result", "");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //calculating progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //displaying percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });

    }


    private void startCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "LEFT");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void picfromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String imageurl = getRealPathFromURI(imageUri);
                    // setVehicleImage(imageurl, requestCode);
                    Log.e("", imageurl);
                    setImage(imageurl);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Try Again");
                }

            } else {
                showToast("Capture Cancelled");
            }


        }

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            setImage(bitmap);

        } else {

            Log.i("SonaSys", "resultCode: " + resultCode);
            switch (resultCode) {
                case 0:
                    Log.i("SonaSys", "User cancelled");
                    break;


            }

        }
    }


    private void setImage(String uristr) {

        Bitmap bitmap = null;
        try {
            if (uristr != null) {
                bitmap = getBitmap(uristr);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Bitmap bt=Bitmap.createScaledBitmap(bitmap, 720, 1100, false);
                Bitmap bt = BITMAP_RESIZER(bitmap, 300, 350);
                bt.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] vehicleImage = stream.toByteArray();
                fileView = getImageUri(this, bt);
                imglist.add(fileView);
                bindView(fileView, true);

            }
        } catch (Exception e) {

        }
    }

    private void setImage(Bitmap bitmap) {

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Bitmap bt=Bitmap.createScaledBitmap(bitmap, 720, 1100, false);
            Bitmap bt = BITMAP_RESIZER(bitmap, 300, 350);
            bt.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] vehicleImage = stream.toByteArray();
            fileView = getImageUri(this, bt);
            imglist.add(fileView);
            bindView(fileView, false);


        } catch (Exception e) {

        }
    }

    private void bindView(Uri uri, boolean flag) {
        LinearLayout layout1 = (LinearLayout) findViewById(R.id.lay1);

        ImageView imageView = new ImageView(this);
        CardView cardView = new CardView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(5, 5, 5, 5);
        cardView.setLayoutParams(params);

        Glide.with(this)
                .load(uri)
                .asBitmap()
                .override(300, 500)
                .fitCenter()
                .into(imageView);
        if (flag) {
            imageView.setRotation(90);
        }
        cardView.addView(imageView);
        layout1.addView(cardView);


    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestPermissions();
        Intent intent = new Intent(this, LocationUpdateService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        getData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    public void onClick(View view) {

    }

    private boolean check() {
        if (imageRequest.getUserName() == null || imageRequest.getUserName().equals("")) {
            showToast("Please Enter User Name");
            return false;
        }
        if (imageRequest.getPlaceName() == null || imageRequest.getPlaceName().equals("")) {
            showToast("Please Enter Place Name");
            return false;
        }
        if (imageRequest.getDescription() == null || imageRequest.getDescription().equals("")) {
            showToast("Please Enter Description");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save_info, menu);//Menu Resource, Menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                uploadImage();
                return true;

        }
        return true;
    }

     private void getData(){
         //Query phoneQuery =databaseReference.orderByChild("lattitude");
         databaseReference.child("location_db").addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 Map<String, LocationRequest> td = new HashMap<String, LocationRequest>();
                 for (DataSnapshot Snapshot: dataSnapshot.getChildren()) {
                     LocationRequest locationRequest = Snapshot.getValue(LocationRequest.class);
                     lat.setText(locationRequest.getLattitude().toString());
                     longit.setText(locationRequest.getLongitude().toString());
                     td.put(Snapshot.getKey(), locationRequest);
                 }

                 ArrayList<LocationRequest> values = new ArrayList<>(td.values());
                 List<String> keys = new ArrayList<String>(td.keySet());

                 for (LocationRequest job: values) {
                     Log.d("firebase", job.getLattitude().toString());
                 }
             }
             @Override
             public void onCancelled(DatabaseError databaseError) {
                 Log.e("Exception", "onCancelled", databaseError.toException());
             }
         });
     }



    public ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            LocationUpdateService.LocalBinder binder = (LocationUpdateService.LocalBinder) service;
            myservice = binder.getService();
            bound = true;
            myservice.setCallbacks(MainActivity.this);
          /*  myService.setCallbacks(DashboardActivity.this); // register
            myService.setCount(DashboardActivity.this);*/
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void doSomething() {
        Log.e("Update","Updated");
        getData();
    }
}
