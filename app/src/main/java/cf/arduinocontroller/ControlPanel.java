package cf.arduinocontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

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
    private PopupWindow popUp;
    private ListView deviceListView;
    ArrayAdapter devicesAdapter;



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
        mBluetoohthService = new BluetoothService();
        if (!mBluetoohthService.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.control_panel, menu);
        MenuItem button = menu.findItem(R.id.device_lookup);
        devicesAdapter = new ArrayAdapter(this, R.layout.device_list_textview,
                mBluetoohthService.getBluetoothDevices());
        LayoutInflater layoutInflater =
                (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_device_list, null);
        deviceListView = (ListView)popupView.findViewById(R.id.device_list);
        deviceListView.setAdapter(devicesAdapter);
        deviceListView.setClickable(false);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice selectedItem = (BluetoothDevice) parent.getItemAtPosition(position);
                mBluetoohthService.connect(selectedItem);
            }
        });
        popUp = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        popUp.setFocusable(true);
        popUp.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        popUp.setOutsideTouchable(true);
        button.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                popUp.showAtLocation(findViewById(R.id.right), Gravity.CENTER, 0, 0);
                return true;
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


