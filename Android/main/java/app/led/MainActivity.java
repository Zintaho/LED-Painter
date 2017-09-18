package app.led;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener
{
    private static final String COLOR_PREFERENCE_KEY = "color";
    private static final int REQUEST_ENABLE_BT = 1;

    Thread mThread;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket;
    OutputStream mOutputStream;
    InputStream mInputStream;
    Set<BluetoothDevice> mDevices;
    int mDevicesCount = 0;

    Button btnColorPicker;
    Button btnClear;
    GridView gridView;

    int pickedColor = Color.rgb(254,12,32);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkBluetooth();
        selectDevice();

    }

    @Override
    protected void onStart()
    {
        super.onStart();


        btnColorPicker = (Button)findViewById(R.id.button);
        btnClear = (Button)findViewById(R.id.button2);
        btnColorPicker.setOnClickListener(mClickListener);
        btnClear.setOnClickListener(mClickListener2);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new GridAdapter(this));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.getBackground() == null) {
                    view.setBackground(getResources().getDrawable(R.drawable.cell, null));
                }
                if (view.getBackground() != null) {
                    view.setBackgroundColor(pickedColor);
                    int test = Math.abs(pickedColor);
                    char Red = (char) (255 - ((test & 0xFF0000) >> 16));
                    char Green = (char) (255 - ((test & 0x00FF00) >> 8));
                    char Blue = (char) (256 - ((test & 0x0000FF)));
                    char index = (char) (position);

                    Log.d("Color", "" + (int) Red + " " + (int) Green + " " + (int) Blue + "[" + (int) index + "]");

                    sendData(Red);
                    SystemClock.sleep(10);
                    sendData(Green);
                    SystemClock.sleep(10);
                    sendData(Blue);
                    SystemClock.sleep(10);
                    sendData(index);
                    SystemClock.sleep(10);

                }
            }
        });
    }

    Button.OnClickListener mClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
                    int color = PreferenceManager.getDefaultSharedPreferences(
                            MainActivity.this).getInt(COLOR_PREFERENCE_KEY, Color.WHITE);
                    new ColorPickerDialog(MainActivity.this, MainActivity.this,
                            color).show();
        }
    };

    Button.OnClickListener mClickListener2 = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            clearAll();
        }
    };

    @Override
    public void colorChanged(int color)
    {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(COLOR_PREFERENCE_KEY,
                color).commit();
        pickedColor = color;
    }

    protected void clearAll()
    {
        if(gridView.getAdapter() != null)
        {
            gridView.setAdapter(new GridAdapter(this));

            sendData(0);
            SystemClock.sleep(10);
            sendData(0);
            SystemClock.sleep(10);
            sendData(0);
            SystemClock.sleep(10);
            sendData(64);
            SystemClock.sleep(10);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
            {
                if(resultCode == RESULT_OK)
                {
                    //블루투스가 활성상태로 변경됨.
                }
                else
                {
                    finish();
                }

                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void  onDestroy()
    {
        try
        {
            mThread.interrupt();
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        }
        catch (Exception e)
        {

        }
        super.onDestroy();
    }

    void checkBluetooth()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null)
        {
            //지원하지 않는 경우
            finish();
        }
        else
        {
            //블루투스를 지원하는 경우
            if(!mBluetoothAdapter.isEnabled())
            {//비활성인경우 사용자 동의 요청
                Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btIntent,REQUEST_ENABLE_BT);
            }
        }


    }

    void selectDevice()
    {
         /*1. 페어링 목록 불러오기*/
        mDevices = mBluetoothAdapter.getBondedDevices();
        mDevicesCount = mDevices.size();
        if(mDevicesCount == 0)
        {
            //페어링 장치 갯수가 0인경우
            finish();
        }

        /*2. 다이얼로그 생성*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("장치를 선택하십시오.");

        /*3. 목록 생성*/
        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices)
        {
            listItems.add(device.getName());
        }
        listItems.add("취소");

        /*4. 목록 입력 및 동작 설정*/
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == mDevicesCount) {
                            //취소
                            finish();
                        } else {
                            //연결 시도
                            connectToSelectedDevices(items[item].toString());
                        }
                    }
                });

        /*5. 목록 출력*/
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    void connectToSelectedDevices(String selectedDeviceName)
    {
        mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try
        {
            //소켓 생성
            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();

            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
        }
        catch(Exception e)
        {
            //연결 중 오류 발생
            finish();
        }
    }

    BluetoothDevice getDeviceFromBondedList(String name)
    {
        BluetoothDevice selectedDevice = null;

        for(BluetoothDevice device : mDevices)
        {
            if(name.equals(device.getName()))
            {
                selectedDevice = device;
                break;
            }
        }

        return selectedDevice;
    }

    void sendData(int msg)
    {
        try
        {
            mOutputStream.write(msg);
        }
        catch (Exception e)
        {
            finish();
        }
    }
}
