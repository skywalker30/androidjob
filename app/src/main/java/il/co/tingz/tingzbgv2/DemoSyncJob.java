package il.co.tingz.tingzbgv2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.evernote.android.job.Job;

import java.util.Calendar;
import java.util.Random;

/**
 * @author rwondratschek
 */
public class DemoSyncJob extends Job {

    public static final String TAG = "job_demo_tag";
    private LocationManager mLocationManager;
    private Params params;
    @Override
    @NonNull
    protected Result onRunJob(@NonNull final Params params) {
        boolean success = new DemoSyncEngine(getContext()).sync();
        this.params = params;

        if (isProviderEnabled(getContext())) {

            if (mLocationManager == null)
                mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return Result.FAILURE;
            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

        } else {
            return Result.FAILURE;
        }



        return success ? Result.SUCCESS : Result.FAILURE;
    }


    static boolean isProviderEnabled(Context context) {
        Log.i(TAG, "isProviderEnabled" );
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                && lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    @SuppressLint("RestrictedApi")
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Log.i(TAG, "Update GPS Location: accuracy="+location.getAccuracy());
            mLocationManager.removeUpdates(mLocationListener);
            String str = String.format("%f : %f | %s\n", location.getLatitude(),
                    location.getLongitude(), AppUtils.dateFormat(Calendar.getInstance().getTime(), "dd/MM HH:mm"));
            Log.i(TAG, str);
            String log = AppUtils.getPreferences(getContext(),MainActivity.POINTS_LOG);
            AppUtils.setPreferences(getContext(), MainActivity.POINTS_LOG, str+log);


            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(TAG, "Job Demo", NotificationManager.IMPORTANCE_LOW);
                channel.setDescription("Job demo job");
                getContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(getContext(), TAG)
                    .setContentTitle("ID " + params.getId())
                    .setContentText(str)
                    .setAutoCancel(true)
                    .setChannelId(TAG)
                    .setSound(null)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_notification)
                    .setShowWhen(true)
                    .setColor(Color.GREEN)
                    .setLocalOnly(true)
                    .build();

            NotificationManagerCompat.from(getContext()).notify(new Random().nextInt(), notification);



        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged: "+s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled: "+s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.i(TAG, "onProviderDisabled: "+s);
            mLocationManager.removeUpdates(mLocationListener);

        }
    };




}
