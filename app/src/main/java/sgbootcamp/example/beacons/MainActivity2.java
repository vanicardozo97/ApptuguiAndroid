package sgbootcamp.example.beacons;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity implements SensorEventListener{

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private float deviceOrientation;

    // Administrador del sensor del dispositivo
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa los sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Iniciar el sensor
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener el sensor
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // obtiene datos del acelerometro y magnometro
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values.clone();
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values.clone();

        // Se necesita los datos de ambos sensores para la orientacion
        if ((mGravity != null) && (mGeomagnetic != null)) {
             /* Para la orientaci??n del dispositivo, se debe calcular el azimut:
             *Determina la direcci??n de un cuerpo celeste.
             * Por ejemplo, un cuerpo celeste que se halla al Norte
             * tiene un azimut de 0??, uno al Este 90??,
             * uno al Sur 180?? y al Oeste 270??.
             *
             * Para calcular el azimut, primero se debe obtener la matrix de rotaci??n(usando
             * los datos del acelerometro y magnometro) y despues de obtener la matrix se
             * usa para obtener el azimut
             */
            SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mGeomagnetic);
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimut = orientation[0]; // la orientaci??n contiene el valor de azimut
            // Azimut a grados
            deviceOrientation = (float)(Math.toDegrees(azimut)+360)%360;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
