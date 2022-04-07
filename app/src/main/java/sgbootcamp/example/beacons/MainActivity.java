

package sgbootcamp.example.beacons;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sgbootcamp.example.beacons.Volley.CustomRequest;
import sgbootcamp.example.beacons.Volley.Helper;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,TextToSpeech.OnInitListener, BeaconConsumer,
 RangeNotifier {

    protected final String TAG = MainActivity.this.getClass().getSimpleName();;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final long DEFAULT_SCAN_PERIOD_MS = 30000;
    private static final String ALL_BEACONS_REGION = "AllBeaconsRegion";

    // Para interactuar con los beacons desde una actividad
    private BeaconManager mBeaconManager;
    private TextToSpeech tts;
    private Boolean reproduciendo,encendido;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private float deviceOrientation;

    // Administrador del sensor del dispositivo
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Display display;

    // Representa el criterio de campos con los que buscar beacons
    private Region mRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStartButton().setOnClickListener(this);
        //getStopButton().setOnClickListener(this);

        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        // Fijar un protocolo beacon, Eddystone en este caso
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        //Intervalo de escaneo
        mBeaconManager.setForegroundScanPeriod(2000l);
        mBeaconManager.setBackgroundScanPeriod(2000l);
        mBeaconManager.setForegroundBetweenScanPeriod(1100l);
        mBeaconManager.setBackgroundBetweenScanPeriod(1100l);

        // Inicializa los sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();


        ArrayList<Identifier> identifiers = new ArrayList<>();

        mRegion = new Region(ALL_BEACONS_REGION, identifiers);

    }

    @Override
    public void onClick(View view) {

        if (view.equals(findViewById(R.id.empieza))) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Si los permisos de localización todavía no se han concedido, solicitarlos
                if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

                    askForLocationPermissions();

                } else { // Permisos de localización concedidos

                    prepareDetection();
                }

            } else { // Versiones de Android < 6

                prepareDetection();
            }
            // }else if (view.equals(findViewById(R.id.stopReadingBeaconsButton))) {

            // stopDetectingBeacons();

            //  BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // Desactivar bluetooth
            // if (mBluetoothAdapter.isEnabled()) {
            //    mBluetoothAdapter.disable();
            }
        }

    private void obtieneBeacon(String identificador, final Double distancia) {
        Volley.newRequestQueue(this).add(new CustomRequest(Request.Method.GET, "http://beagui.aquisar.com/public/api/v1/beacons/"+identificador, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject jsonobject = response;
                try {
                    JSONArray beacon = jsonobject.getJSONArray("beacon");
                                                     iniciaTexto(beacon,distancia);
                  } catch (JSONException e) {
                    Log.e(MainActivity.class.getName(), e.toString());
                }
            }
            }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyerror) {
                Log.e(MainActivity.class.getName(), volleyerror.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-type","application/json");
                return params;
            }}
        );
    }

    private void iniciaTexto(JSONArray beacon, Double distancia){
        class Mensaje implements Runnable {
            JSONArray b;
            Double d;
            Mensaje(JSONArray beacon, Double distancia) { b = beacon; d = distancia; }
           public void run() {
            muestraMensaje(b,d);
           }
            }
        Thread t = new Thread(new Mensaje(beacon,distancia));
        t.start();
    }


    private void muestraMensaje(JSONArray beacon, final Double distancia) {
        try {
            String nombre = beacon.getJSONObject(0).getString("descripcion");

            int metros = (int) Math.floor(distancia);
            int centimetros = (int) Math.floor(((distancia-metros)*100));

            reproduceTexto("Te encuentras aproximadamente a "+String.valueOf(metros)+"punto"+String.valueOf(centimetros)+" metros de "+nombre);


            JSONArray obstaculos = beacon.getJSONObject(0).getJSONArray("barreras");
            Helper helper = new Helper();
            for (int i = 0; i<obstaculos.length(); i++){
                JSONObject obstaculo = obstaculos.getJSONObject(i);
                String  tipo = obstaculo.getJSONObject("tipo_barrera").getString("tipo");
                String posicion = obstaculo.getString("posicion");

                String orientacionDispositivo = helper.obtieneOrientacion(deviceOrientation);
                String referencia = helper.obtieneReferencia(orientacionDispositivo, posicion);

                String contexto = "   ";

                if(referencia.equals("Izquierda") || referencia.equals("Derecha")){
                    contexto = " a la ";
                }

                int metros_obstaculo = (int) Math.floor(Double.parseDouble(obstaculo.getString("distancia")));
                int centimetros_obstaculo = (int) Math.floor(((Double.parseDouble(obstaculo.getString("distancia"))-metros_obstaculo)*100));

                reproduceTexto("Hay un "+tipo+contexto+referencia+"  "+nombre+"  aproximadamente a "+String.valueOf(metros_obstaculo)+"punto"+String.valueOf(centimetros_obstaculo)+"    metros");
            }

            reproduciendo = false;

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private boolean reproduceTexto(String texto){
        tts.setSpeechRate(0.8f);
        tts.speak(texto,TextToSpeech.QUEUE_FLUSH, null,"id1");
        boolean speaking = tts.isSpeaking();
        do{
            speaking = tts.isSpeaking();
        }while (speaking);

        return true;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            Locale locSpanish = new Locale("spa", "PAR");
            int result = tts.setLanguage(locSpanish);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    /**
     * Activar localización y bluetooth para empezar a detectar beacons
     */
    private void prepareDetection() {

        if (!isLocationEnabled()) {

            askToTurnOnLocation();

        } else { // Localización activada, comprobemos el bluetooth

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {

                showToastMessage(getString(R.string.not_support_bluetooth_msg));

            } else if (mBluetoothAdapter.isEnabled()) {

                startDetectingBeacons();

            } else {

                // Pedir al usuario que active el bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

            // Usuario ha activado el bluetooth
            if (resultCode == RESULT_OK) {

                startDetectingBeacons();

            } else if (resultCode == RESULT_CANCELED) { // User refuses to enable bluetooth

                showToastMessage(getString(R.string.no_bluetooth_msg));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Empezar a detectar los beacons, ocultando o mostrando los botones correspondientes
     */
    private void startDetectingBeacons() {

        //Iniciar y apagar el sistema
        encendido = true;
        // Fijar un periodo de escaneo
        tts = new TextToSpeech(MainActivity.this, MainActivity.this);

        // Enlazar al servicio de beacons. Obtiene un callback cuando esté listo para ser usado
        mBeaconManager.bind(this);
        iniciaVibracion(1000);
        // Desactivar botón de comenzar
        getStartButton().setEnabled(false);
        getStartButton().setAlpha(.5f);

        // Activar botón de parar
        //getStopButton().setEnabled(true);
        //getStopButton().setAlpha(1);
    }
    private void iniciaVibracion(long tiempo) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(tiempo);
    }

    @Override
    public void onBeaconServiceConnect() {

        try {
            // Empezar a buscar los beacons que encajen con el el objeto Región pasado, incluyendo
            // actualizaciones en la distancia estimada
            mBeaconManager.startRangingBeaconsInRegion(mRegion);

            showToastMessage("Buscando Beacons");

        } catch (RemoteException e) {
            Log.d(TAG, "Se ha producido una excepción al empezar a buscar beacons " + e.getMessage());
        }

        mBeaconManager.addRangeNotifier(this);
    }


    /**
     * Método llamado cada DEFAULT_SCAN_PERIOD_MS segundos con los beacons detectados durante ese
     * periodo
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
            for (Beacon beacon : beacons){
                double distancia = beacon.getDistance();
                String identificador = beacon.getId1().toString();
                if(distancia<12){
                    if(!reproduciendo){
                        reproduciendo = true;
                        obtieneBeacon(identificador,distancia);
                    }
                }
            }
        }
    }

    /**
     * Comprobar permisión de localización para Android >= M
     */
    private void askForLocationPermissions() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_access_needed);
        builder.setMessage(R.string.grant_location_access);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareDetection();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.funcionality_limited);
                    builder.setMessage(getString(R.string.location_not_granted) +
                            getString(R.string.cannot_discover_beacons));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    /**
     * Comprobar si la localización está activada
     *
     * @return true si la localización esta activada, false en caso contrario
     */
    private boolean isLocationEnabled() {

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        boolean networkLocationEnabled = false;

        boolean gpsLocationEnabled = false;

        try {
            networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
            Log.d(TAG, "Excepción al obtener información de localización");
        }

        return networkLocationEnabled || gpsLocationEnabled;
    }

    /**
     * Abrir ajustes de localización para que el usuario pueda activar los servicios de localización
     */
    private void askToTurnOnLocation() {

        // Notificar al usuario
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.location_disabled);
        dialog.setPositiveButton(R.string.location_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.show();
    }

    private ImageView getStartButton() {
        return (ImageView) findViewById(R.id.empieza);
    }

    /*private Button getStopButton() {
        return (Button) findViewById(R.id.stopReadingBeaconsButton);
    }*/

    /**
     * Mostrar mensaje
     *
     * @param message mensaje a enseñar
     */
    private void showToastMessage (String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.unbind(this);
    }
}
