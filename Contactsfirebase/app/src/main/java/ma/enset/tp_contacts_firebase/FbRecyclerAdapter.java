package ma.enset.tp_contacts_firebase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FbRecyclerAdapter extends FirebaseRecyclerAdapter<Contact , FbRecyclerAdapter.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FbRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Contact> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contact model) {
        holder.full_name.setText(model.getFirst_name()+" "+model.getLast_name());
        holder.phone.setText(model.getPhone());
        Glide.with(holder.image.getContext())
                .load(model.getImage_url())
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(holder.image);


        //Show update dialog when clicking a contact card
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.image.getContext())
                        .setContentHolder(new ViewHolder(R.layout.activity_edit))
                        .setExpanded(true,1000)
                        .create();

                //dialogPlus.show();

                View view = dialogPlus.getHolderView();

                EditText firstName = view.findViewById(R.id.firstName_edit_text);
                EditText lastName = view.findViewById(R.id.lastName_edit_text);
                EditText phone = view.findViewById(R.id.phone_edit_text);
                EditText email = view.findViewById(R.id.email_edit_text);
                CircleImageView circleImageView = view.findViewById(R.id.contact_image);

                FloatingActionButton btnSaveEdited = view.findViewById(R.id.saveBtn);

                firstName.setText(model.getFirst_name());
                lastName.setText(model.getLast_name());
                email.setText(model.getEmail());
                phone.setText(model.getPhone());
                Glide.with(circleImageView.getContext())
                        .load(model.getImage_url())
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_person_24)
                        .into(circleImageView);

                dialogPlus.show();

                btnSaveEdited.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Map<String, Object> map =new HashMap<>();
                        map.put("first_name",firstName.getText().toString());
                        map.put("last_name",lastName.getText().toString());
                        map.put("email",email.getText().toString());
                        map.put("phone",phone.getText().toString());
                        map.put("image_url","");

                        FirebaseDatabase.getInstance().getReference().child("contacts")
                                .child(getRef(position).getKey()).updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(holder.full_name.getContext(), "Data Edited Successfully", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(holder.full_name.getContext(), "Error while Editing", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();
                                    }
                                });
                    }
                });

                //Delete action
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.full_name.getContext());
                        builder.setTitle("Are you sure");

                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase.getInstance().getReference().child("contacts")
                                        .child(Objects.requireNonNull(getRef(position).getKey())).removeValue();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(holder.full_name.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();

                            }
                        });
                        builder.show();
                    }
                });

            }
        });


    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        CircleImageView image;
        TextView full_name, phone;

        FloatingActionButton btnDelete;
        CardView cardView;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.ivPhoto);
            full_name = itemView.findViewById(R.id.contact_name_tv);
            phone = itemView.findViewById(R.id.phone_number_tv);

            btnDelete = itemView.findViewById(R.id.delete_fab);
            cardView = itemView.findViewById(R.id.contact_item);


        }
    }
}
