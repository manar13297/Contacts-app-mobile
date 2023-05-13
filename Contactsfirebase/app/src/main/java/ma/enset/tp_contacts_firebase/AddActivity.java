package ma.enset.tp_contacts_firebase;
import android.content.Intent;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContentInfo;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddActivity  extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 2;
    private EditText firstNameEt, lastNameEt, phoneEt, emailEt;
    private CircleImageView contactImg;


    private Uri imageUri;

    private FloatingActionButton backBtn , saveBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);


        firstNameEt =findViewById(R.id.firstName_edit_text);
        lastNameEt = findViewById(R.id.lastName_edit_text);
        phoneEt = findViewById(R.id.phone_edit_text);
        emailEt = findViewById(R.id.email_edit_text);
        contactImg = findViewById(R.id.contact_image);
        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertData();
            }
        });
        
        contactImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode==RESULT_OK){
            imageUri = data.getData();
            uploadPhoto();
        }
    }

    private void uploadPhoto() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("uploading");
        pd.show();

        if(imageUri!=null){
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("contacts").child(String.valueOf(System.currentTimeMillis())+"."+getFileExtension(imageUri));
            fileRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Log.d("Download Url", url);
                            pd.dismiss();
                            Toast.makeText(AddActivity.this,"Image uploaded Successfully",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });


        }
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void insertData() {
        //fetch data from all text fields, store themin a variable, pass them in a map and send them to the database
        Map<String,Object> map = new HashMap<>();
        map.put("first_name",firstNameEt.getText().toString());
        map.put("last_name",lastNameEt.getText().toString());
        map.put("email",emailEt.getText().toString());
        map.put("phone",phoneEt.getText().toString());
        map.put("image_url","");
        if(phoneEt.getText().toString().equals("")||firstNameEt.getText().toString().equals("")){
            Toast.makeText(AddActivity.this,"first name and phone number are required",Toast.LENGTH_SHORT).show();

        }else{
            FirebaseDatabase.getInstance().getReference().child("contacts").push()
                    .setValue(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(AddActivity.this,"Data inserted Successfully",Toast.LENGTH_SHORT).show();
                            firstNameEt.setText("");
                            lastNameEt.setText("");
                            emailEt.setText("");
                            phoneEt.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddActivity.this,"Error while insertion",Toast.LENGTH_SHORT).show();

                        }
                    });
        }



    }

}
