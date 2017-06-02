package prawas.exa.com.prawas_journy.listner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Swapnil on 18/04/2017.
 */

public class GpsLocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            Toast.makeText(context, "Gps Setting Changed",
                    Toast.LENGTH_SHORT).show();

        }
    }
}
