package com.teamrevelador.firebasedemo;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private int RC_PHOTO_PICKER=101;
    Uri selectedImageUri;
    boolean imageSelected = false;
    CircleImageView circleImageView;
    EditText nameEditText, ageEditText;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseFirestore db;
    private String TAG= "FIRESTORELOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleImageView= findViewById(R.id.main_circle_image_view);
        nameEditText = findViewById(R.id.main_name_edit_text);
        ageEditText = findViewById(R.id.main_age_edit_text);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();


        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
    }

    private void openImagePicker() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, RC_PHOTO_PICKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                selectedImageUri = data.getData();
                imageSelected = true;
                circleImageView.setImageURI(selectedImageUri);
                uploadImageToStorage();
            }
        }
    }

    private void uploadImageToStorage(){
        storageRef
                .child(selectedImageUri.getLastPathSegment())
                .putFile(selectedImageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Toast.makeText(MainActivity.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                        String downloadUrl = task.getResult().getDownloadUrl()+"";
                        Map<String, Object> city = new HashMap<>();
                        city.put("name", nameEditText.getText().toString());
                        city.put("age", ageEditText.getText().toString());
                        city.put("photoUrl", downloadUrl);

                        db.collection("user").document("user1")
                                .set(city)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Firestore Success", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                    }
                }).
                addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAGGER", e.getMessage());
                Toast.makeText(MainActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
