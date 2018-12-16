package il.co.tingz.tingzbgv2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private int mLastJobId;
    private JobManager mJobManager;
    private static final String LAST_JOB_ID = "LAST_JOB_ID";
    public final static String POINTS_LOG = "points.log";
    private TextView logTv;

    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JobManager.create(this).addJobCreator(new DemoJobCreator());

        mJobManager = JobManager.instance();

        if (savedInstanceState != null) {
            mLastJobId = savedInstanceState.getInt(LAST_JOB_ID, 0);
        }


        logTv = findViewById(R.id.textView_log);
        logTv.setText(AppUtils.getPreferences(getApplicationContext(), POINTS_LOG));

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        startButton.setOnClickListener(startButtonListener);
        stopButton.setOnClickListener(stopButtonListener);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TedPermission.with(this)
                    .setPermissionListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            if (!isFinishing() && !isDestroyed()) {

                            }
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                        }
                    })
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).check();
        } else {

        }
    }

    private void testPeriodic() {
        mLastJobId = new JobRequest.Builder(DemoSyncJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule();
    }
    private void testCancelAll() {
        mJobManager.cancelAll();
    }

    private View.OnClickListener startButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            testPeriodic();
        }
    };

    private View.OnClickListener stopButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            testCancelAll();
        }
    };
}
