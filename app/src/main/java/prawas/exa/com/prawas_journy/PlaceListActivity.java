package prawas.exa.com.prawas_journy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import prawas.exa.com.prawas_journy.Common.CommonActivity;
import prawas.exa.com.prawas_journy.model.ImageRequest;

/**
 * Created by root on 25/5/17.
 */

public class PlaceListActivity extends CommonActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_place_list);

        init();

       /* databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ImageRequest c = dataSnapshot.getValue(ImageRequest.class);
                Log.d("Categories: ", c.getDescription() + " " + c.getPlaceName());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        databaseReference.child("place_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, ImageRequest>> genericTypeIndicator = new GenericTypeIndicator<Map<String, ImageRequest>>() {};
                Map<String, ImageRequest> map = dataSnapshot.getValue(genericTypeIndicator );
                System.out.print(""+"");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(MainActivity.this, "Failed to read value.", error.toException());
                Log.e("",error.getMessage());
            }
        });
    }
}
