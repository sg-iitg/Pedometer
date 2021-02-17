package com.j4velin.pedometer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class LeaderboardView extends Fragment {
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    String uuid;
    ArrayList<LeaderBoard> leaderBoardArrayList;
    LeaderAdapter leaderAdapter;
    GoogleSignInClient mGoogleSignInClient;
    RecyclerView recyclerView;
    Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        //like if the class is HomeFragment it should have R.layout.home_fragment
        //if it is DashboardFragment it should have R.layout.fragment_dashboard
        View view = inflater.inflate(R.layout.activity_leaderboard, container, false);
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recyclerview);
        firebaseDatabase = FirebaseDatabase.getInstance();
        uuid = mAuth.getUid();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);


        leaderBoardArrayList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getData();
        leaderAdapter = new LeaderAdapter(leaderBoardArrayList);
        recyclerView.setAdapter(leaderAdapter);
        return view;

    }

    private void getData() {
        leaderBoardArrayList.clear();
        Query DatabaseQuery = firebaseDatabase.getReference().child("USERS").orderByChild("Time").limitToLast(100);
        DatabaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaderBoardArrayList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        if (snapshot2.getKey().equals("Name")) {
                            String name = snapshot2.getValue().toString();
                            Log.d("vipin",name);
                            leaderBoardArrayList.add(new LeaderBoard(name, name, name, name));
                        }

                    }
                    leaderAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*DatabaseQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                leaderBoardArrayList.clear();
                String name="PPPPPPP";
                String time="PPPPPPP";
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    if(dataSnapshot.getKey().equals("Name")) {
                        name = dataSnapshot.getValue().toString();
                        Log.d("name",name);
                    }
                    if(dataSnapshot.getKey().equals("Time"))
                        time=dataSnapshot.getValue().toString();
                    Log.d("vipin",dataSnapshot.getValue().toString());
                    if(!name.equals("PPPPPPP") && !time.equals("PPPPPPP"))
                        leaderBoardArrayList.add(new LeaderBoard(name,time,time,time));

                }
                leaderAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                leaderBoardArrayList.clear();
                String name="PPPPPPP";
                String time="PPPPPPP";
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {

                    if(dataSnapshot.getKey().equals("Name")) {
                        name = dataSnapshot.getValue().toString();
                        Log.d("name",name);
                    }
                    if(dataSnapshot.getKey().equals("Time"))
                        time=dataSnapshot.getValue().toString();
                    Log.d("vipin",dataSnapshot.getValue().toString());
                        if(!name.equals("PPPPPPP") && !time.equals("PPPPPPP"))
                        leaderBoardArrayList.add(new LeaderBoard(name,time,time,time));

                }
                leaderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }*/


    }
}

