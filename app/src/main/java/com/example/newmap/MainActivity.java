package com.example.newmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newmap.WebService.Asynchtask;
import com.example.newmap.WebService.WebService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener, Asynchtask {

    GoogleMap mapa;
    ArrayList<LatLng> puntosMarcados = new ArrayList<>();
    Polygon poligono;

    Double sumaTotal =0.00;

    PolylineOptions lineas;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mapa.getUiSettings().setZoomControlsEnabled(true);

        LatLng madrid = new LatLng(40.689606890693504, -74.04514418270492);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(45)
                .tilt(70)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        mapa.animateCamera(camUpd3);
        mapa.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        MarkerOptions marcador = new MarkerOptions();
        marcador.position(latLng);
        marcador.title("Punto");
        mapa.addMarker(marcador);
        puntosMarcados.add(latLng);
        PolygonOptions polygonOptions = null;

        //contabilizar los 6 puntos
        if (puntosMarcados.size() >= 6) {
          polygonOptions = new PolygonOptions()
                    .addAll(puntosMarcados)
                    .strokeColor(Color.RED);
            poligono = mapa.addPolygon(polygonOptions);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng punto : puntosMarcados) {
                builder.include(punto);
            }

        //contabilizar los 6 puntos

        for (int i = 0; i<polygonOptions.getPoints().size()-1; i++){
            String url="https://maps.googleapis.com/maps/api/distancematrix/json?destinations="+polygonOptions.getPoints().get(i).latitude+", "+polygonOptions.getPoints().get(i).longitude+"&origins="+polygonOptions.getPoints().get(i+1).latitude+", "+polygonOptions.getPoints().get(i+1).longitude+"&units=meters&key=AIzaSyAZmpF3k0bcm-3c-f_0feLZQZRwYu-gdr0";
            Log.i("ojo", polygonOptions.toString());
            Map<String, String> datos = new HashMap<String, String>();
            WebService ws= new
                    WebService(url,
                    datos, MainActivity.this, MainActivity.this);
            ws.execute("GET");
        }
      }
    }
    @Override
    public void processFinish(String result) throws JSONException {
        try {
            JSONObject jsonObjeto = new JSONObject(result);
            JSONArray jsonArrgelo = jsonObjeto.getJSONArray("rows");
            JSONObject jsonElementos = jsonArrgelo.getJSONObject(0);
            JSONArray jsonNuevo = jsonElementos.getJSONArray("elements");
            JSONObject jsonReferencia = jsonNuevo.getJSONObject(0);
            JSONObject JSONDISTANCIA = jsonReferencia.getJSONObject("distance");
            String distancia = JSONDISTANCIA.getString("text");
            Log.i("distancia", distancia);
            Double distanciaMetros = Double.parseDouble(distancia.replace(" m", ""));
            sumaTotal += distanciaMetros;

            Log.i("distanciatotal", sumaTotal.toString());

            textView.setText("Distancia total es: "+sumaTotal.toString()+"km");

        }
        catch(Exception e) {
            textView.setText(e.toString());


        }
        }
}

