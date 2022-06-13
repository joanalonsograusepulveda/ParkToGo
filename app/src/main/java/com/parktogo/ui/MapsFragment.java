package com.parktogo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parktogo.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;

    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference BD_PARQUES;
    private ArrayList<LatLng> coordenasParque = new ArrayList<>();
    private ArrayList<String> nombreParque = new ArrayList<>();
    private ArrayList<String> deporteParque = new ArrayList<>();
    private SupportMapFragment mapFragment;

    private Bitmap basketball;
    private Bitmap football;
    private Bitmap tennis_ball;
    private Bitmap volleyball;
    private Bitmap gym;
    private Bitmap skatepark;
    private Bitmap pets;
    private Bitmap petanca;

    private View popup = null;
    private String posicion;
    private ImageView img;
    private ArrayList<String> imagenParque = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        getLocalizacion();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        BD_PARQUES = firebaseDatabase.getReference("BD Parques");
        Consulta();
    }

    private void Consulta(){
        BD_PARQUES.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float latitud;
                double longitud;
                LatLng parque;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String name = snapshot.child("Nombre").getValue(String.class);
                    nombreParque.add(name);
                    String image = snapshot.child("Imagen").getValue(String.class);
                    imagenParque.add(image);
                    String sport = snapshot.child("Deporte").getValue(String.class);
                    deporteParque.add(sport);
                    latitud = Float.parseFloat(snapshot.child("Latitud").getValue().toString());
                    longitud = Double.parseDouble(snapshot.child("Longitud").getValue().toString());
                    parque = new LatLng(latitud,longitud);
                    coordenasParque.add(parque);
                }
                mapFragment.getMapAsync(callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {

            imagenesDeportes();

            mMap = googleMap;
            LatLng cullera = new LatLng(39.1666700,-0.2500000);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cullera,15));
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Nullable
                @Override
                public View getInfoContents(@NonNull Marker marker) {
                    if(popup == null){
                        popup=getLayoutInflater().inflate(R.layout.custom_info_windows,null);
                    }

                    TextView name = (TextView) popup.findViewById(R.id.nombre);
                    img = (ImageView) popup.findViewById(R.id.imagen);
                    TextView dep = (TextView) popup.findViewById(R.id.deporte);

                    String[] split = marker.getId().split("m");
                    posicion = split[1];

                    name.setText(marker.getTitle());
                    dep.setText(marker.getSnippet());

                    Picasso.get()
                            .load(imagenParque.get(Integer.parseInt(posicion)))
                            .into(img);

                    return popup;
                }

                @Nullable
                @Override
                public View getInfoWindow(@NonNull Marker marker) {
                    return null;
                }
            });

            for (int i = 0; i < coordenasParque.size(); i++) {
                switch (deporteParque.get(i)){
                    case "Basquet":
                        mMap.addMarker(new MarkerOptions()
                            .position(coordenasParque.get(i))
                            .icon(BitmapDescriptorFactory.fromBitmap(basketball))
                                .snippet(deporteParque.get(i))
                            .title(nombreParque.get(i)));
                        break;
                    case "Football":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(football))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                    case "Voley":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(volleyball))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                    case "Tenis":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(tennis_ball))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                    case "Skatepark":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(skatepark))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                    case "Ejercicio":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(gym))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                    case "Animales":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(pets))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                    case "Petanca":
                        mMap.addMarker(new MarkerOptions()
                                .position(coordenasParque.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(petanca))
                                .snippet(deporteParque.get(i))
                                .title(nombreParque.get(i)));
                        break;
                }
            }
        }
    };

    private void imagenesDeportes() {
        int height = 150;
        int width = 150;

        BitmapDrawable bitmapdrawBasquet = (BitmapDrawable) getResources().getDrawable(R.drawable.basketball);
        Bitmap b = bitmapdrawBasquet.getBitmap();
        basketball = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDrawable bitmapdrawFootball = (BitmapDrawable) getResources().getDrawable(R.drawable.football);
        Bitmap f = bitmapdrawFootball.getBitmap();
        football = Bitmap.createScaledBitmap(f, width, height, false);
        BitmapDrawable bitmapdrawTenis = (BitmapDrawable) getResources().getDrawable(R.drawable.tennis_ball);
        Bitmap t = bitmapdrawTenis.getBitmap();
        tennis_ball = Bitmap.createScaledBitmap(t, 100, 100, false);
        BitmapDrawable bitmapdrawVoley = (BitmapDrawable) getResources().getDrawable(R.drawable.volleyball);
        Bitmap v = bitmapdrawVoley.getBitmap();
        volleyball = Bitmap.createScaledBitmap(v, 110, 110, false);
        BitmapDrawable bitmapdrawSkatepark = (BitmapDrawable) getResources().getDrawable(R.drawable.skatepark);
        Bitmap s = bitmapdrawSkatepark.getBitmap();
        skatepark = Bitmap.createScaledBitmap(s, width, height, false);
        BitmapDrawable bitmapdrawAnimeles = (BitmapDrawable) getResources().getDrawable(R.drawable.animales);
        Bitmap a = bitmapdrawAnimeles.getBitmap();
        pets = Bitmap.createScaledBitmap(a, width, height, false);
        BitmapDrawable bitmapdrawEjercicio = (BitmapDrawable) getResources().getDrawable(R.drawable.gym);
        Bitmap e = bitmapdrawEjercicio.getBitmap();
        gym = Bitmap.createScaledBitmap(e, width, height, false);
        BitmapDrawable bitmapdrawPetanca = (BitmapDrawable) getResources().getDrawable(R.drawable.petanca);
        Bitmap p = bitmapdrawPetanca.getBitmap();
        petanca = Bitmap.createScaledBitmap(p, width, height, false);
    }

    private void getLocalizacion() {
        int permiso = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permiso == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)){

            }else{
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }
}