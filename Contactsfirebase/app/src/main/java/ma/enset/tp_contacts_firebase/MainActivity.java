package ma.enset.tp_contacts_firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ma.enset.tp_contacts_firebase.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private RecyclerView response_recycler_view;
    private FbRecyclerAdapter fbRecyclerAdapter;

    private FloatingActionButton fab_save_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Recyclerview
        response_recycler_view = findViewById(R.id.response_recycler_view);
        response_recycler_view.setLayoutManager(new LinearLayoutManager(this));


        //FirebaseRecyclerAdapter
        Query query = FirebaseDatabase.getInstance().getReference().child("contacts");
        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(query, Contact.class)
                        .build();

        fbRecyclerAdapter = new FbRecyclerAdapter(options);
        response_recycler_view.setAdapter(fbRecyclerAdapter);

        fab_save_data = (FloatingActionButton) findViewById(R.id.fab);
        fab_save_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext() ,AddActivity.class));
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        fbRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fbRecyclerAdapter.stopListening();
    }


    //For search Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                textSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                textSearch(s);
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    private void textSearch(String str) {
        Query q = FirebaseDatabase.getInstance().getReference().child("contacts").orderByChild("first_name").startAt(str).endAt(str+"~");
        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(q, Contact.class)
                        .build();

        fbRecyclerAdapter = new FbRecyclerAdapter(options);
        fbRecyclerAdapter.startListening();
        response_recycler_view.setAdapter(fbRecyclerAdapter);
    }
}