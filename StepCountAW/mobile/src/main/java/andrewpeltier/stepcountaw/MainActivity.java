package andrewpeltier.stepcountaw;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "MainActivity(Mobile)";
    private static final String HEADER = "Date, Time, Step Count, Accuracy";
    private static final String STEP_COUNTER_DATA_ITEM = "/step_counter";
    private static final String STEP_COUNTER_VALUE = "/step_counter_value";
    private static final String STEP_COUNTER_ACCURACY = "/step_counter_accuracy";
    private String filename;

    private final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,     // Required for Local I/O Operations
            Manifest.permission.WAKE_LOCK,                  // Required for MQTT Paho Library
    };
    private static final int PERMISSION_CODE = 111;

    private Context mContext;
    private GoogleApiClient googleApiClient;
    private Button          toggleButton;
    private TextView        userText;
    private boolean         record = false;
    private int steps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        toggleButton = findViewById(R.id.toggleBtn);
        toggleButton.setText("Start");
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filename != null)
                {
                    record = !record;
                    Log.e(TAG, "Connected? ---- " + googleApiClient.isConnected());
                    Log.e(TAG, "Recording? " + record);
                    if(record)
                    {
                        toggleButton.setText("Stop");
                    }
                    else
                    {
                        userText.setText("Waiting...");
                        toggleButton.setText("Start");
                        steps = 0;
                    }
                }
                else
                {
                    Toast.makeText(mContext, "Please set a filename in the menu", Toast.LENGTH_SHORT).show();
                }

            }
        });

        userText = findViewById(R.id.userText);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer)
    {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(STEP_COUNTER_DATA_ITEM) && record) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    int hrVal = dataMap.getInt(STEP_COUNTER_VALUE);
                    steps++;
                    int scAcc = dataMap.getInt(STEP_COUNTER_ACCURACY);
                    Log.d(TAG, "Heart Rate: " + steps + '\t' + "Accuracy: " + scAcc);

                    userText.setText("" + steps);
                    Date date = Calendar.getInstance().getTime();
                    String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
                    String timeString = new SimpleDateFormat("kk:mm:ss:SSS", Locale.US).format(date);

                    String data = dateString + "," + timeString  + "," + steps + "," + scAcc;
                    if(filename != null)
                    DataLogService.log(mContext, new File(GenerateDirectory.getRootFile(), filename), data, HEADER);
                }
            }
        }
    }

    private void setFilename()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("File Name");
        builder.setMessage("Please enter your file name (Do not enter extension): ");

        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                filename = input.getText().toString();
                Toast.makeText(mContext, "Name Saved.", Toast.LENGTH_SHORT);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_file:
                setFilename();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Check Permissions at Runtime (Android M+), and Request if Necessary
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The Results from this Request are handled in a Callback Below.
            requestPermissions(PERMISSIONS, PERMISSION_CODE);
        }
    }

    @Override
    protected void onDestroy()
    {
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // TODO: Handle event Permissions denied
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "Connected to GoogleAPIClient!");
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Api Client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google Api Client connection failed");
    }
}
