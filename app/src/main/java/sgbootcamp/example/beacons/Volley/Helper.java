package sgbootcamp.example.beacons.Volley;

/**
 * Created by aquinoedmair on 06/12/17.
 */
public class Helper {

    public String obtieneOrientacion(float deviceOrientation){
        String orientacion = "";
        if((deviceOrientation>300 && deviceOrientation<=360) || (deviceOrientation>=0 && deviceOrientation<=40)){
            orientacion = "Norte";
        }else if(deviceOrientation>=240 && deviceOrientation<=290){
            orientacion = "Oeste";
        }else if(deviceOrientation>=150 && deviceOrientation<=220){
            orientacion = "Sur";
        }else {
            orientacion = "Este";
        }

        return orientacion;
    }

    public String obtieneReferencia(String orientacion, String posicion){
        String referencia = "";
        if(orientacion.equals("Sur") && posicion.equals("Oeste")){
            referencia = "Derecha";
        }else if(orientacion.equals("Sur") && posicion.equals("Este")){
            referencia = "Izquierda";
        }else if(orientacion.equals("Sur") && posicion.equals("Norte")){
            referencia = "Atrás";
        }else if(orientacion.equals("Sur") && posicion.equals("Sur")){
            referencia = "Enfrente";
        }else if(orientacion.equals("Norte") && posicion.equals("Oeste")){
            referencia = "Izquierda";
        }else if(orientacion.equals("Norte") && posicion.equals("Este")){
            referencia = "Derecha";
        }else if(orientacion.equals("Norte") && posicion.equals("Norte")){
            referencia = "Enfrente";
        }else if(orientacion.equals("Norte") && posicion.equals("Sur")){
            referencia = "Atrás";
        }else if(orientacion.equals("Este") && posicion.equals("Oeste")){
            referencia = "Atrás";
        }else if(orientacion.equals("Este") && posicion.equals("Este")){
            referencia = "Enfrente";
        }else if(orientacion.equals("Este") && posicion.equals("Norte")){
            referencia = "Izquierda";
        }else if(orientacion.equals("Este") && posicion.equals("Sur")){
            referencia = "Derecha";
        }else if(orientacion.equals("Oeste") && posicion.equals("Oeste")){
            referencia = "Enfrente";
        }else if(orientacion.equals("Oeste") && posicion.equals("Este")){
            referencia = "Atrás";
        }else if(orientacion.equals("Oeste") && posicion.equals("Norte")){
            referencia = "Derecha";
        }else if(orientacion.equals("Oeste") && posicion.equals("Sur")){
            referencia = "Izquierda";
        }

        return  referencia;
    }

}
