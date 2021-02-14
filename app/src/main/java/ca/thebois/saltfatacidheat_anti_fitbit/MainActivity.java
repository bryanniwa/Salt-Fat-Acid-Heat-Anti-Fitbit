package ca.thebois.saltfatacidheat_anti_fitbit;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean activityTrackingEnabled;
    private final boolean runningQOrLater =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    private List<ActivityTransition> activityTransitionList;
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";
    private PendingIntent activityTransitionPendingIntent;
    private TransitionsReceiver transitionsReceiver;
    private final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = AddMealActivity.makeLaunchIntent(this);
            startActivity(i);
        });

        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        activityTrackingEnabled = false;

        populateTransitionList();
        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        activityTransitionPendingIntent =
                PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        transitionsReceiver = new TransitionsReceiver();

        getPermissionsIfNotGranted();
        createNotificationChannel();

        if (activityRecognitionPermissionApproved()) {
            enableActivityTransitions();
        }
    }

    private void getPermissionsIfNotGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }
    }

    private void populateTransitionList() {
        activityTransitionList = new ArrayList<>();
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        );
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(transitionsReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (activityRecognitionPermissionApproved()) {
                enableActivityTransitions();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableActivityTransitions() {
        ActivityTransitionRequest request = new ActivityTransitionRequest(activityTransitionList);

        Task<Void> task =
                ActivityRecognition.getClient(this)
                        .requestActivityTransitionUpdates(request, activityTransitionPendingIntent);

        task.addOnSuccessListener(aVoid -> {
            activityTrackingEnabled = true;
            Toast.makeText(this, "enabled activity tracking", Toast.LENGTH_SHORT).show();
        });

        task.addOnFailureListener(aVoid ->
                Toast.makeText(this, "enabled activity tracking FAILED", Toast.LENGTH_SHORT).show());
    }

    private boolean activityRecognitionPermissionApproved() {
        if (runningQOrLater) {
            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }

    public class TransitionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (ActivityTransitionResult.hasResult(intent)) {
                Toast.makeText(MainActivity.this,"detected walking", Toast.LENGTH_SHORT).show();
                notifyMotionDetected();
            }
        }
    }

    private void notifyMotionDetected() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                "motionAlertNotification")
                .setSmallIcon(R.drawable.ic_baseline_airline_seat_recline_normal_24)
                .setContentTitle("Time to sit down")
                .setContentText("You've been moving too long. Time to rest")
                .setCategory(Notification.CATEGORY_ALARM);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Motion alert";
            String description = "Sends a notification when the user is found to be walking";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("motionAlertNotification",
                    name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(transitionsReceiver);
        super.onStop();
    }
}