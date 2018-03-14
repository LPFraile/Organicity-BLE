package com.redbear.chat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;

import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import android.view.MenuItem;

import android.view.Window;

import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	private final static String TAG = Main.class.getSimpleName();


	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 100;

	public static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

	private ArrayList<BluetoothDevice> devices;
	private String DEVICE_NAME = "name";
	private String DEVICE_ADDRESS = "address";


 	private String devicename="Blend";//Limit to 10 characters


	private TextView tv = null;
	private String mDeviceName;
	private String mDeviceAddress;

	private Map<UUID, BluetoothGattCharacteristic> mapa = new HashMap<UUID, BluetoothGattCharacteristic>();

	private Handler handler = new Handler();

	private int NumDevices;
	private int NumDevicesReceive;


	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;

	private BluetoothGatt mBluetoothGatt;

	public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";
	public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "EXTRA_DATA";

	public final static UUID UUID_BLE_SHIELD_TX = UUID
			.fromString(RBLGattAttributes.BLE_SHIELD_TX);
	public final static UUID UUID_BLE_SHIELD_RX = UUID
			.fromString(RBLGattAttributes.BLE_SHIELD_RX);
	public final static UUID UUID_BLE_SHIELD_SERVICE = UUID
			.fromString(RBLGattAttributes.BLE_SHIELD_SERVICE);



	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.i(TAG, "Ble device has finish of send variables:");

				Log.i(TAG, "the device has disconnect");
				DisconnectWithDevice();
				NumDevicesReceive++;
				Log.i(TAG,"Number of devices received until now:"+NumDevicesReceive);
				//connect with a other device until has been receive the variables from all of them
				if (NumDevicesReceive<NumDevices) {
					mDeviceName=mDevices.get(NumDevicesReceive).getName();
					mDeviceAddress=mDevices.get(NumDevicesReceive).getAddress();
					Log.i(TAG,String.valueOf(NumDevicesReceive)+" device:"+mDeviceName+"Adress"+mDeviceAddress);
					ConnectWithDevice();
				}
				else if(NumDevicesReceive==(NumDevices))// all the device have send their variables
				{
					Log.i(TAG, "Has been receive variables from every accesable device");
				}

			} else if (ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				getGattService(getSupportedGattService());

			} else if (ACTION_DATA_AVAILABLE.equals(action)) {
				ReceiveData(intent.getByteArrayExtra(EXTRA_DATA));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		tv = (TextView) findViewById(R.id.textView);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());

		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		//initialize();
		handler.post(runnableCode);

	}



	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
							 byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (device != null) {
						Log.i(TAG, "deviceaddr:" + device.toString());
						Log.i(TAG, "devicename"+ device.getName());
						//If exist device
						if (device.getName() != null) {
							//I take just one time each device that containt Blend on the name
							if (device.getName().contains(devicename)&&!mDevices.contains(device)) {
								mDevices.add(device);
								Log.i(TAG, "Add device:" + device.getName());
							}
						}
					}
				}
			});
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth
		Log.i(TAG, "OnActivityresult");
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	protected void onResume() {
		Log.w(TAG, "OnResume");
		super.onResume();

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "OnOtionItemSelected");
		if (item.getItemId() == android.R.id.home) {
			Log.i(TAG, "Here");
			disconnect();
			close();

			System.exit(0);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "OnStop");
		super.onStop();


	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Ondistroy");
		super.onDestroy();
		handler.removeCallbacks(runnableCode);
		disconnect();
		close();
	}

	private void FindDevice() {

		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {

				mBluetoothAdapter.startLeScan(mLeScanCallback);
				try {
					Thread.sleep(SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//When stop the scan
				mBluetoothAdapter.stopLeScan(mLeScanCallback);

				//I initialize the counter of receive device to 0
				NumDevicesReceive = 0;
				//I initialize the number of devices that has been scan
				NumDevices = mDevices.size();

				//I choose the first device and connect with iy
				if (!mDevices.isEmpty()) {
					mDeviceName=mDevices.get(NumDevicesReceive).getName();
					mDeviceAddress=mDevices.get(NumDevicesReceive).getAddress();

					ConnectWithDevice();
				}
			}
		}, SCAN_PERIOD);


	}

	private void ReceiveData(byte[] byteArray) {
		if (byteArray != null) {
			String data = new String(byteArray);
			Log.i(TAG,"RECEIVED:"+data);

			if (data.charAt(0) == 'V') {//Receive a Variable

				int VarNameLenght = byteArray[1];


				String VarName=data.substring(1,VarNameLenght+2);

				Log.i(TAG,"Name of receive variable:"+ VarName);

				byte floatdata[]=new byte[4];
				floatdata[0]=byteArray[VarNameLenght+2];
				floatdata[1]=byteArray[VarNameLenght+3];
				floatdata[2]=byteArray[VarNameLenght+4];
				floatdata[3]=byteArray[VarNameLenght+5];
				ByteBuffer bufferfloat = ByteBuffer.wrap(floatdata);
				float Value = bufferfloat.getFloat(); //This helps to
				//int Value=byteArray[VarNameLenght+3] << 8| 0x00 << 24 | byteArray[VarNameLenght+2]& 0xff;

				Log.i(TAG, "Value of receive variable" + String.valueOf(Value));
				tv.append(VarName);
				tv.append(String.valueOf(Value));
				//Send an ask for a new Variable
				BluetoothGattCharacteristic characteristic = mapa
						.get(UUID_BLE_SHIELD_TX);

				byte b = 0x00;

				byte[] tx = new byte[3];
				tx[0] = b;
				tx[1] = 'V';
				tx[2] = 'n';
				Log.i(TAG, "Send command:" + tx[1]);
				characteristic.setValue(tx);
				writeCharacteristic(characteristic);


			}
			else if (data.charAt(0) == 'G') {//Receive conexion confirm

				Log.i(TAG, "Ble device is connect:");

				BluetoothGattCharacteristic characteristic = mapa
						.get(UUID_BLE_SHIELD_TX);


				byte b = 0x00;

				//Send a command asking for a first variable

				byte[] tx = new byte[3];
				tx[0] = b;
				tx[1] = 'V';
				tx[2] = 'n';
				Log.i(TAG, "Send command:" + tx[1]);
				characteristic.setValue(tx);
				writeCharacteristic(characteristic);

			}

			// find the amount we need to scroll. This works by
			// asking the TextView's internal layout for the position
			// of the final line and then subtracting the TextView's height
			final int scrollAmount = tv.getLayout().getLineTop(
					tv.getLineCount())
					- tv.getHeight();
			// if there is no need to scroll, scrollAmount will be <=0
			if (scrollAmount > 0)
				tv.scrollTo(0, scrollAmount);
			else
				tv.scrollTo(0, 0);
		}
	}


//run periodical to scan for devices
	private Runnable runnableCode = new Runnable() {


		@Override
		public void run() {

			//Initialize all the conexion
		if (mBluetoothGatt!=null)
		{
			Log.i(TAG, "Was not disconnected the before bluthoot disconnect now");
			disconnect();
			close();
		}
			//mBluetoothManager=null;


			Log.i(TAG, "Periodic conection");
			mDevices.clear();


			mDeviceName = null;
			mDeviceAddress = null;

			if (!mapa.isEmpty())
				mapa.clear();

			tv.clearComposingText();

			//Find Blend devices
			FindDevice();

			Log.e("Handlers", "Called");

			// Repeat this runnable code again every 10 seconds
			handler.postDelayed(runnableCode,20000);
		}

	};
	private void ConnectWithDevice(){
		if (!initialize()) {
			Log.e(TAG, "Unable to initialize Bluetooth");
			finish();
		}
		connect(mDeviceAddress);
		Log.i(TAG,"Connect with the device with:");
		Log.i(TAG,"Address:"+mDeviceAddress);
		Log.i(TAG, "Name:" + mDeviceName);

	}
	private void DisconnectWithDevice(){

		Log.i(TAG,"Disconnect with the device with:");
		Log.i(TAG, "Address:" + mDeviceAddress);
		Log.i(TAG, "Name:" + mDeviceName);

		disconnect();
		close();
	}


	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;


		BluetoothGattCharacteristic characteristic = gattService
				.getCharacteristic(UUID_BLE_SHIELD_TX);
		mapa.put(characteristic.getUuid(), characteristic);

		BluetoothGattCharacteristic characteristicRx = gattService
				.getCharacteristic(UUID_BLE_SHIELD_RX);
		setCharacteristicNotification(characteristicRx,
				true);
		readCharacteristic(characteristicRx);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(ACTION_GATT_CONNECTED);
		intentFilter.addAction(ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(ACTION_DATA_AVAILABLE);

		return intentFilter;
	}
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			String intentAction;
			Log.i(TAG,"onconectionstatechange....");

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				Log.i(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
				//close();
			}
		}

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_RSSI, rssi);
			} else {
				Log.w(TAG, "onReadRemoteRssi received: " + status);
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}
	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, int rssi) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
								 final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid())) {
			final byte[] rx = characteristic.getValue();
			intent.putExtra(EXTRA_DATA, rx);
		}AN_PERIOD

		sendBroadcast(intent);
	}


	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetootAN_PERIODhAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address
	 *            The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mDeviceAddress != null
				&& address.equals(mDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mDeviceAddress = address;

		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if ( mBluetoothAdapter == null) {
			Log.w(TAG, "BluetoothAdapter not initialized0");
			return;
		}
		else if (mBluetoothGatt == null)
		{
			Log.w(TAG,"mBluetoothGAt is null");
			return;
		}
		mBluetoothGatt.disconnect();
		Log.i(TAG,"Disconnect has happened");
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		Log.i(TAG,"The close has happened");
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized1");
			return;
		}

		mBluetoothGatt.readCharacteristic(characteristic);
	}


	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized3");
			return;
		}

		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized4");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID
							.fromString(RBLGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public BluetoothGattService getSupportedGattService() {

		if (mBluetoothGatt == null) {

			return null;
		}
		return mBluetoothGatt.getService(UUID_BLE_SHIELD_SERVICE);
	}
}





