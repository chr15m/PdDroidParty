package cx.mccormick.pddroidparty;

import java.lang.Object;

import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.util.Log;

public class DroidSystem extends Widget implements SensorEventListener {
	private static final String TAG = "DroidSystem";
	private SensorManager sensorManager;
	private Vibrator vibrator;

	public DroidSystem(PdDroidPatchView app, String[] atomline) {
		super(app);

		sendname = app.app.replaceDollarZero(atomline[5]) + "-snd";
		receivename = app.app.replaceDollarZero(atomline[5]) + "-rcv";
		setupreceive();

		sensorManager = (SensorManager)parent.app.getSystemService(Context.SENSOR_SERVICE);
		vibrator = (Vibrator)parent.app.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void receiveMessage(String symbol, Object... args) {
		if(symbol.equals("gotoUrl")) {
			if ((args.length == 1) && args[0].getClass().equals(String.class)) {
				Uri uriUrl = Uri.parse(args[0].toString());
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
				parent.app.startActivity(launchBrowser);
			}
			return ;
		}
		else if(symbol.equals("sensors")) {
			if ((args.length == 2)
			        && args[0].getClass().equals(String.class)
			        && args[1].getClass().equals(String.class)) {
				int SensorType, SensorDelay;

				if(args[0].toString().equals("accel")) SensorType = Sensor.TYPE_ACCELEROMETER;
				else if(args[0].toString().equals("gyro")) SensorType = Sensor.TYPE_GYROSCOPE;
				else if(args[0].toString().equals("magn")) SensorType = Sensor.TYPE_MAGNETIC_FIELD;
				else return;

				sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(SensorType));

				if(args[1].toString().equals("normal")) SensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
				else if(args[1].toString().equals("fast")) SensorDelay = SensorManager.SENSOR_DELAY_GAME;
				else if(args[1].toString().equals("fastest")) SensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
				else return;

				sensorManager.registerListener(this, sensorManager.getDefaultSensor(SensorType), SensorDelay);
			}
			return ;
		}
		else if(symbol.equals("vibr")) {
			vibrator.vibrate(((Float)args[0]).longValue());
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		// check sensor type
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			send("accel " + x + " " + y + " " + z);
		}
		else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			send("gyro " + x + " " + y + " " + z);
		}
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			send("magn " + x + " " + y + " " + z);
		}
	}
}
