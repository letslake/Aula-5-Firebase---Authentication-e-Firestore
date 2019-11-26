package com.example.exaula05;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.exaula05.DateHelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List <Mensagem> mensagens;
    private EditText mensagemEditText;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double currentLatitude;
    private double currentLongitude;
    private static final int REQUEST_CODE_GPS = 1001;
    private static final int GPS_DISTANCE = 200;
    private static final int GPS_TIME = 20000;
    private CollectionReference mMsgsReference;
    private FirebaseUser fireUser;

    private void setupFirebase(){
        mMsgsReference =
                FirebaseFirestore.getInstance().collection(
                        "mensagens"
                );
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        mMsgsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mensagens.clear();
                for (DocumentSnapshot document :
                        queryDocumentSnapshots.getDocuments()){
                    Mensagem m = document.toObject(Mensagem.class);
                    mensagens.add(m);
                }
                Collections.sort(mensagens);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupRecyclerView (){
        mensagensRecyclerView = findViewById(R.id.mensagensRecyclerView);
        mensagens = new ArrayList<>();
        adapter = new ChatAdapter (this, mensagens);
        mensagensRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        mensagensRecyclerView.setAdapter(adapter);
    }

    private void setupLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private void setupViews (){
        mensagemEditText =
                findViewById(R.id.mensagemEditText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setupRecyclerView();
        setupViews();
        setupFirebase();
        setupLocation();
    }

    public void enviarMensagem (View v){
        String texto = mensagemEditText.getText().toString();
        Mensagem m = new Mensagem (fireUser.getEmail(), new Date(), texto);
        mMsgsReference.add(m);
        mensagemEditText.setText("");
        Toast.makeText(
                this,
                getString(R.string.msg_enviada),
                Toast.LENGTH_SHORT
        ).show();
    }

    public void enviarGps (View v){
        String texto = String.format("geo:%f,%f", currentLatitude, currentLongitude);
        Mensagem m = new Mensagem (fireUser.getEmail(), new Date(), texto);
        mMsgsReference.add(m);
        mensagemEditText.setText("");
        Toast.makeText(
                this,
                getString(R.string.msg_enviada),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GPS_TIME, GPS_DISTANCE, locationListener);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME, GPS_DISTANCE, locationListener);
                }
            } else {
                Toast.makeText(this, getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
            }
        }
    }

}

class ChatAdapter extends RecyclerView.Adapter <ChatViewHolder>{

    private Context context;
    private List<Mensagem> mensagens;

    public ChatAdapter(Context context, List<Mensagem> mensagens) {
        this.context = context;
        this.mensagens = mensagens;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View raiz = inflater.inflate(
                R.layout.list_item,
                parent,
                false
        );
        return new ChatViewHolder(raiz);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Mensagem m = mensagens.get(position);
        holder.mensagemTextView.setText(
                m.getTexto()
        );
        holder.dataNomeTextView.setText(
                context.getString(
                        R.string.data_nome,
                        DateHelper.format(m.getData()),
                        m.getUsuario()
                )
        );
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }
}

class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView dataNomeTextView;
    TextView mensagemTextView;

    public ChatViewHolder (View raiz){
        super (raiz);
        dataNomeTextView =
                raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView =
                raiz.findViewById(R.id.mensagemTextView);
    }
}