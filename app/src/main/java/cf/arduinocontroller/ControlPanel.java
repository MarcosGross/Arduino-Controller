package cf.arduinocontroller;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.Objdetect;

public class ControlPanel extends Activity {



    private BluetoothService mBluetoohthService;
    private Objdetect objdetect;
    ArrayAdapter devicesAdapter;



    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    setContentView(R.layout.activity_control_panel);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    static{ System.loadLibrary("opencv_java"); }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {
                    mBluetoohthService.AddDevice(device);
                }
                catch (Exception e){}
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);
        ActionBar mActionBar = getActionBar();
        mBluetoohthService = new BluetoothService();
        if (!mBluetoohthService.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        devicesAdapter = new ArrayAdapter(this, R.layout.spinner_item,
                mBluetoohthService.getBluetoothDevices());
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        objdetect = new Objdetect();
        objdetect.groupRectangles(new MatOfRect(),new MatOfInt(),1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.control_panel, menu);
        MenuItem menuItem = menu.findItem(R.id.DeviceMenu);
        Spinner spinner = (Spinner)menuItem.getActionView();
        spinner.setAdapter(devicesAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice selectedItem =(BluetoothDevice) adapterView.getSelectedItem();
                mBluetoohthService.connect(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return true;
    }



    public void controlButtonClicked(View view){
        switch (view.getId()) {
            case R.id.forward:
                mBluetoohthService.write("f".getBytes());
                break;
            case R.id.left:
                mBluetoohthService.write("l".getBytes());
                break;
            case R.id.right:
                mBluetoohthService.write("r".getBytes());
                break;
            case R.id.stop:
                mBluetoohthService.write("s".getBytes());
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}


