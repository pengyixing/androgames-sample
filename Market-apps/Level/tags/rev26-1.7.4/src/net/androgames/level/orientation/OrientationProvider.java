package net.androgames.level.orientation;

import java.util.List;

import net.androgames.level.Level;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 
 * A Bubble level for Android phones
 * 
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey
 *
 */
public abstract class OrientationProvider implements SensorEventListener {

    private Sensor sensor;
    private SensorManager sensorManager;
	private OrientationListener listener;
 
    /** indicates whether or not Accelerometer Sensor is supported */
    private Boolean supported;
    /** indicates whether or not Accelerometer Sensor is running */
    private boolean running = false;

	/** Calibration */
	private float calibratedPitch;
	private float calibratedRoll;
	
	/** Screen orientation fix */
	protected int screenConfig;
	
	/** Orientation */
    protected float pitch;
    protected float roll;
    protected float tmp;
	private Orientation orientation;
 
    /**
     * Returns true if the manager is listening to orientation changes
     */
    public boolean isListening() {
        return running;
    }
 
    /**
     * Unregisters listeners
     */
    public void stopListening() {
        running = false;
        try {
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {}
    }
 
    protected abstract int getSensorType();
    
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public boolean isSupported() {
        if (supported == null) {
            if (Level.getContext() != null) {
                sensorManager = (SensorManager) Level.getContext().getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensors = sensorManager.getSensorList(getSensorType());
                return sensors.size() > 0;
            }
        }
        return false;
    }
 
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public void startListening(OrientationListener orientationListener) {
        sensorManager = (SensorManager) Level.getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(getSensorType());
        if (sensors.size() > 0) {
            sensor = sensors.get(0);
            running = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            listener = orientationListener;
        }
    }
    
    protected abstract void handleSensorChanged(SensorEvent event);

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	public void onSensorChanged(SensorEvent event) {
		handleSensorChanged(event);
            
        pitch -= getCalibratedPitch();
        roll -= getCalibratedRoll();
 
        if (pitch < -45 && pitch > -135) {
            // top side up
            orientation = Orientation.TOP;
        } else if (pitch > 45 && pitch < 135) {
            // bottom side up
            orientation = Orientation.BOTTOM;
        } else if (roll > 45) {
            // right side up
            orientation = Orientation.RIGHT;
        } else if (roll < -45) {
            // left side up
            orientation = Orientation.LEFT;
        } else {
        	// landing
        	orientation = Orientation.LANDING;
        }
            
        getListener().onOrientationChanged(orientation, pitch, roll);
	}
    
    protected OrientationListener getListener() {
		return listener;
	}

	/**
	 * Reset the calibration
	 */
	public final void resetCalibration() {
		calibratedPitch = 0;
		calibratedRoll = 0;
	}

	public final void setCalibration(float...values) {
		calibratedPitch += values[0];
		calibratedRoll += values[1];
	}

	public float getCalibratedPitch() {
		return calibratedPitch;
	}

	public float getCalibratedRoll() {
		return calibratedRoll;
	}

	public void setScreenConfig(int screenConfig) {
		this.screenConfig = screenConfig;
	}
	
}
