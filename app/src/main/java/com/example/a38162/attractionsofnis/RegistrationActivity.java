package com.example.a38162.attractionsofnis;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.Task;

public class RegistrationActivity extends AppCompatActivity {
    DatabaseReference databaseReferences;
    FirebaseDatabase firebaseDatabase;
    Integer REQUEST_CAMERA=0, SELECT_FILE=1;
    File imageFile;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    String picture;
    User user;
    Uri uriProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddUser();
            }
        });

        databaseReferences = firebaseDatabase.getInstance().getReference().child("users");

        Button button = (Button) findViewById(R.id.button_picture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        storageReference = firebaseStorage.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        user = new User();
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Sign in", "signInAnonymously:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Sign in", "signInAnonymously:failure", task.getException());
                    Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }

                // ...
            }
        });
    }

    private void SelectImage() {
        final CharSequence[] items= {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
                else if(items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, SELECT_FILE);
                }
                else if(items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path) {

        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = (ImageView) findViewById(R.id.image_user);
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "image " + user.name, null);
        return Uri.parse(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ImageView image = (ImageView) findViewById(R.id.image_user);
                image.setImageBitmap(bitmap);

                int check = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (check == PackageManager.PERMISSION_GRANTED) {
                    saveToInternalStorage(bitmap);
                    Bitmap bitmap1 = (Bitmap) data.getExtras().get("data");
                    Uri uri = getImageUri(RegistrationActivity.this, bitmap1);
                    uriProfileImage = uri;
                    picture = uri.toString();
                    uploadImgToFirebaseStorage();
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
                    }
                }
            else if(requestCode == SELECT_FILE) {
                Uri imageUri = data.getData();
                uriProfileImage = imageUri;
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageView image = (ImageView) findViewById(R.id.image_user);
                image.setImageBitmap(bitmap);

                uploadImgToFirebaseStorage();
            }
        }
    }

    private void AddUser() {
        EditText name = findViewById(R.id.edit_name);
        EditText surname = findViewById(R.id.edit_surname);
        EditText phone = findViewById(R.id.edit_phone);
        EditText email = findViewById(R.id.edit_email);
        EditText username = findViewById(R.id.edit_username);
        EditText password = findViewById(R.id.edit_password);

        final String Name = name.getText().toString();
        final String Surname = surname.getText().toString();
        final String Phone = phone.getText().toString();
        final String Email = email.getText().toString();
        final String Username = username.getText().toString();
        final String Password = password.getText().toString();

        if(TextUtils.isEmpty(Name)) {
            Toast.makeText(this, "You have to enter a name", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(Surname)) {
            Toast.makeText(this, "You have to enter a surname", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(Phone)) {
            Toast.makeText(this, "You have to enter a phone number", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(Username)) {
            Toast.makeText(this, "You have to enter a username", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(Email)) {
            Toast.makeText(this, "You have to enter a email", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(Password)) {
            Toast.makeText(this, "You have to enter a password", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    finish();
                    String id = mAuth.getCurrentUser().getUid();
                    user.userId = id;
                    user.name = Name;
                    user.surname = Surname;
                    user.username = Username;
                    user.email = Email;
                    user.password = Password;
                    user.phone_number = Phone;
                    user.score="0";
                    user.visable="1";
                    user.scoringPlaces="no";
                    user.picture = picture;
                    databaseReferences.child(id).setValue(user);

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(mAuth.getCurrentUser().getUid())
                            .setValue(user);

                    /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
                    String token = preferences.getString("registrationToken", "");

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("registrationToken")
                            .setValue(token);*/

                    Toast.makeText(getApplicationContext(), "User Registered Successfully", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    private void uploadImgToFirebaseStorage() {
        StorageReference profileImgRef;

        profileImgRef = FirebaseStorage.getInstance().getReference("Photos/" + System.currentTimeMillis() + ".jpg");

        if(uriProfileImage != null)
        {
            profileImgRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            picture = taskSnapshot.getDownloadUrl().toString();
                            Log.e("Slika", picture);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
    }
}
