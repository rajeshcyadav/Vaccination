package com.example.vaccination.ui.profile;

import static android.content.Context.LOCATION_SERVICE;

import static com.example.vaccination.myutils.MyConstants.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.myInterface.FirebaseImageUploadCallback;
import com.example.vaccination.myInterface.MyTaskCallback;
import com.example.vaccination.myInterface.ProfileDataReceived;
import com.example.vaccination.myInterface.TimeDialogOnSave;
import com.example.vaccination.myutils.EditTextValidator;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.ui.mainscreen.hospital.HospitalMainDirections;
import com.example.vaccination.ui.mainscreen.hospital.HospitalVaccineListDirections;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class HospitalProfile extends Fragment {
    private final String[] myPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int RequestCode = 101;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private static final int PICK_IMAGE_REQUEST = 73;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LoadingDialog loadingDialog, loadingDialogSave;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String TAG = "UserProfile";
    private EditText locationEdittext, name, email, phone;
    private CircleImageView profile_pic;
    private RadioGroup radioGroupTime;
    private String latLong;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private Hospital hospitalProfile;

    private ActivityResultLauncher<String> permissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    getLatLong();
                } else {
                    Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                }
            });


    private final ActivityResultLauncher<Intent> pickImageActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),
                                        data.getData());

                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
                                Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                profile_pic.setImageBitmap(decoded);
                            } catch (IOException e) {
                                Log.d(TAG, "onActivityResult: Error "+e);
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getContext(), "Some error Occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });


    public HospitalProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hospital_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingDialog = new LoadingDialog(getContext(), "Retrieving Location...");
        loadingDialogSave = new LoadingDialog(getContext(), "Uploading Data...");
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        init(view);
    }

    private void init(View root) {
        List<EditText> editTextList = new ArrayList<>();
        Button saveProfile = root.findViewById(R.id.buttonProfileSaveDoc);
        Button signOut = root.findViewById(R.id.buttonProfileLogOutHospital);

        name = root.findViewById(R.id.editTextNameDoc);
        email = root.findViewById(R.id.editTextEmailDoc);
        phone = root.findViewById(R.id.editTextPhoneDoc);
        locationEdittext = root.findViewById(R.id.editTextLocationDoc);
        profile_pic = root.findViewById(R.id.profile_imageDoc);
        radioGroupTime = root.findViewById(R.id.radioGroupHospitalTime);

        editTextList.add(name);
        editTextList.add(email);
        editTextList.add(phone);
        editTextList.add(locationEdittext);


        name.addTextChangedListener(new EditTextValidator(name) {
            @Override
            public void validate(TextView textView, String text) {
                if (text.length() < 3) {
                    textView.setError(NAME_ERROR);
                }
                else{
                    textView.setError(null);
                }
            }
        });

        email.addTextChangedListener(new EditTextValidator(email) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    textView.setError(EMAIL_ERROR);
                }
                else{
                    textView.setError(null);
                }
            }
        });

        phone.addTextChangedListener(new EditTextValidator(phone) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.PHONE.matcher(text).matches()) {
                    textView.setError(PHONE_ERROR);
                }
                else{
                    textView.setError(null);
                }
            }
        });


        profile_pic.setOnClickListener(view -> {
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageActivityResult.launch(intent2);
        });

        locationEdittext.setOnClickListener(view -> myPermission());

        MyTaskCallback dataUploadedCallback = taskSuccess -> {

            if (taskSuccess) {
                NavDirections action = HospitalProfileDirections.actionHospitalProfileToHospitalMain();
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
            }
            if (loadingDialog.isShowing())
                loadingDialog.dismiss();
            if (loadingDialogSave.isShowing())
                loadingDialogSave.dismiss();
        };


        //callback interface when image url is returned
        FirebaseImageUploadCallback firebaseImageUploadCallback = (downloadUri, success) -> {
            if (success) {
                setModel(downloadUri, dataUploadedCallback);
            } else
                loadingDialogSave.dismiss();
        };
        saveProfile.setOnClickListener(view -> {
            loadingDialogSave.show();
            if (validateEditTexts(editTextList)) {
                uploadImage(firebaseImageUploadCallback);
            }
            else
            loadingDialogSave.dismiss();
        });

        signOut.setOnClickListener(view -> {
            auth.signOut();
            NavDirections action = HospitalProfileDirections.actionHospitalProfileToSignIn();
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
        });


        //callback interface when profile data is received
        ProfileDataReceived profileDataReceived = (object, success) -> {

            if (success) {
                hospitalProfile = (Hospital) object;
                setData(hospitalProfile);
            }
            //not adding else case since this can be the first time user is setting up account

        };
        retrieveData(profileDataReceived);


    }


    private void setLatLongToModel(String string, Hospital hospital) {
        String[] split = string.split(",");
        hospital.setLatitude(Double.valueOf(split[0]));
        hospital.setLongitude(Double.valueOf(split[1]));
    }

    //----------------------Validation-----------------//

    private boolean validateEditTexts(List<EditText> editTexts) {
        for (EditText editText : editTexts) {
            if (TextUtils.isEmpty(editText.getText())) {
                editText.setError("Field Can't be empty");
                return false;
            } else
                editText.setError(null);
        }
        return true;
    }


    private void setModel(Uri downloadUri, MyTaskCallback listener) {
        Hospital hospital = new Hospital();

        hospital.setName(name.getText().toString());
        hospital.setEmail(email.getText().toString());
        hospital.setContactNumber(phone.getText().toString());
        hospital.setLocation(locationEdittext.getText().toString());
        hospital.setTime("10 AM - 10 PM");
        hospital.setImgUrl(downloadUri.toString());
        if (latLong != null && !latLong.equals("")) {
            setLatLongToModel(latLong, hospital);
        } else {
            hospital.setLatitude(hospitalProfile.getLatitude());
            hospital.setLongitude(hospitalProfile.getLongitude());
        }
        hospital.setUid(auth.getUid());
        if (hospital.getStars()==0){
            hospital.setStars((Math.random()*4)+1);
        }
        sendData(hospital, listener);
    }


    //--------------------Firebase----------------------------//

    //uploads Img
    private void uploadImage(FirebaseImageUploadCallback firebaseImageUploadCallback) {
        StorageReference profileRef = storageReference.child("ProfileImg/" + auth.getUid());

        Bitmap bitmap = ((BitmapDrawable) profile_pic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot,
                Task<Uri>>) task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return profileRef.getDownloadUrl();
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                firebaseImageUploadCallback.imageUploaded(downloadUri, true);
            } else {
                Toast.makeText(getContext(), "Failed to Upload Image", Toast.LENGTH_SHORT).show();
                firebaseImageUploadCallback.imageUploaded(null, false);
            }
        });


    }


    //upload data  to firebase
    private void sendData(Hospital hospital, MyTaskCallback listener) {
        if (hospital == null) {
            Toast.makeText(getContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection(DbNode.HOSPITAL.toString()).document(auth.getUid()).set(hospital, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection(DbNode.INDEX.toString()).document(auth.getUid()).update("profileCompleted", true);
                        listener.result(true);
                    } else {
                        listener.result(false);
                    }

                }).addOnFailureListener(e -> {
            listener.result(false);
        });
    }

    //retrieve Data
    private void retrieveData(ProfileDataReceived profileDataReceived) {
        db.collection(DbNode.HOSPITAL.toString()).document(auth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profileDataReceived.dataReceived(task.getResult().toObject(Hospital.class), true);
                    } else {
                        profileDataReceived.dataReceived(null, false);
                    }

                }).addOnFailureListener(e -> profileDataReceived.dataReceived(null, false));
    }

    //set data to profile
    private void setData(Hospital hospital) {

        if (hospital == null) {
            return;
        }
        Glide.with(getContext()).load(hospital.getImgUrl())
                .placeholder(R.drawable.ic_baseline_person_24dp)
                .into(profile_pic);
        name.setText(hospital.getName());
        email.setText(hospital.getEmail());
        phone.setText(hospital.getContactNumber());
        if (hospital.isOpen24Hours()) {
            radioGroupTime.check(R.id.radio24Hours);
        } else {
            radioGroupTime.check(R.id.radio12Hours);
        }

        locationEdittext.setText(hospital.getLocation());


    }


    //----------------------Location---------------------------//

    private void myPermission() {

        if (hasPermission(getContext(), myPermissions)) {
            getLatLong();
        } else if (shouldShowRequestPermissionRationale(myPermissions[0])) {
            Toast.makeText(getContext(), "Send user to setting screen", Toast.LENGTH_SHORT).show();
            showAlert(getString(R.string.permission_title), getString(R.string.permission_denied)
                    , 1);
        } else {
            showAlert(getString(R.string.permission_title),
                    getString(R.string.permission_denied_permanently), 0);

        }
    }

    @SuppressLint("MissingPermission")
    private void getLatLong() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            //400==400 meter
            if (location != null && location.hasAccuracy() && location.getAccuracy() <= 400) {
                try {
                    getAddressFromLatLong(location.getLatitude(), location.getLongitude());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                createLocationRequest();
                Log.d(TAG, "onSuccess: FusedLocation is Null or No Accuracy or less than " +
                        "400 meter Accuracy");
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        loadingDialog.show();
        Log.d(TAG,
                "getCurrentLocation: Method" + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Log.d(TAG, "getCurrentLocation: Method After");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    Log.d(TAG, "onLocationResult: Fused" + location.getLatitude());
                    try {
                        getAddressFromLatLong(location.getLatitude(), location.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            }

        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: createLocationRequest");
            getCurrentLocation();

        });
        task.addOnFailureListener(getActivity(), e -> {
            Log.d(TAG, "onFailure: createLocationRequest");
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(getActivity(),
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

    }

    private void getAddressFromLatLong(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        latLong = latitude + "," + longitude;
        locationEdittext.setText(addresses.get(0).getAddressLine(0));

    }

    private boolean hasPermission(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showAlert(String Title, String Message, final int decider) {
        final AlertDialog alert;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(Title);
        builder.setMessage(Message);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            if (decider == 0) {
                permissionResult.launch(myPermissions[0]);
            } else {

                Toast.makeText(getContext(), "Send to setting", Toast.LENGTH_SHORT).show();
                startSetting();
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alert = builder.create();
        alert.show();
    }

    private void startSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}