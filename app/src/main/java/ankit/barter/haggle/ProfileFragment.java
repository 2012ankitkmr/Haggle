package ankit.barter.haggle;

/**
 * Created by Ankit on 17-06-2016.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

public class ProfileFragment extends Fragment {

    final static String USER_URL="https://haggle-64ac4.firebaseio.com/Users";
    final static String LEN_URL="https://haggle-64ac4.firebaseio.com/Pic_Count";
    Firebase mUserRef;
    Firebase mlenRef;

    Map<String, Map<String, String>> Database = null;
    String Pic_Count = null;


    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "EmailKey";
    public static final String loginKey = "loginKey";
    SharedPreferences sharedpreferences;

    private String userChoosenTask;
    private ImageView profimageView;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedpreferences = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        mUserRef = new Firebase(USER_URL);
        mlenRef = new Firebase(LEN_URL);
        refreshDatabase();

        profimageView = (ImageView)rootView.findViewById(R.id.user_profile_photo);

        profimageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_edit_profile);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDialog();
            }
        });


        return rootView;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(getActivity());

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        final Bitmap image = thumbnail;

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        String ret = getPicId();
                        if(ret.equals("pic_"))
                        {
                            progressDialog.dismiss();
                        }else {

                            new ThreadClass("Mythread",getInputStream(image),ret);
                            progressDialog.dismiss();

                        }
                    }
                }, 2000);
    }


    private ByteArrayInputStream getInputStream(Bitmap bm)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        return bs;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

         Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        final Bitmap image = bm;

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        String ret = getPicId();
                        if(ret.equals("pic_"))
                        {
                         progressDialog.dismiss();
                        }else {

                            new ThreadClass("Mythread",getInputStream(image),ret);
                            progressDialog.dismiss();
                        }
                    }
                }, 2000);
    }

    private String getPicId()
    {

        String pic_Id = "pic_";
        String email = sharedpreferences.getString(Email,"abc@gmail.com");

        Map<String, String> userinfo = null;

            refreshDatabase();

            try {
                userinfo = Database.get(email);

                int count = Integer.parseInt(Pic_Count);

                Toast.makeText(getActivity(), String.valueOf(count + 1), Toast.LENGTH_SHORT).show();

                pic_Id = pic_Id.concat(String.valueOf(count + 1));
                User NewUser = new User(userinfo, pic_Id);
                mlenRef.setValue(String.valueOf(count + 1));
                mUserRef.child(email).setValue(NewUser);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unable to upload Please Try again later!", Toast.LENGTH_SHORT).show();
                Log.e("Error", e.toString());
            }
        return pic_Id;
    }

    private void displayDialog()
    {
        final EditText editName,editPhone,editPassword;
        Button saveBtn;
        refreshDatabase();

        final Dialog d=new Dialog(getActivity());

        d.setContentView(R.layout.profile_edit_wizard);
        editName = (EditText) d.findViewById(R.id.edit_changename_editText);
        editPhone = (EditText) d.findViewById(R.id.edit_changephone_edittext);
        editPassword = (EditText) d.findViewById(R.id.edit_changepassword_edittext);


        saveBtn = (Button) d.findViewById(R.id.change_profile_save);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Saving...");
                progressDialog.show();


                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {


                String email = sharedpreferences.getString(Email,"abc@gmail.com");

                Map<String, String> userinfo =null;
                refreshDatabase();
                try {
                   userinfo = Database.get(email);

                String mail = email.replace("_",".");
                String name;
                String password;
                String productListed;
                String phone1;
                String pic_id;

                try {
                    pic_id= userinfo.get("Pic_id");
                }
                catch (Exception e)
                {
                    pic_id = null;
                }

                try {
                    productListed= userinfo.get("ProductListed");
                }
                catch (Exception e)
                {
                    productListed = null;
                }

                if(editName.getText().toString()==null||editName.length()==0)
                {
                    name = userinfo.get("Name");
                }
                else {
                    try {
                        name=editName.getText().toString();
                    }
                    catch (Exception e)
                    {
                        name = "User Name";
                    }
                }

                if(editPassword.getText().toString()==null||editPassword.length()==0)
                {
                    try {
                        password = userinfo.get("Password");
                    }catch (Exception e)
                    {
                        password = null;
                    }

                }
                else {
                    password = editPassword.getText().toString();
                }

                if(editPhone.getText().toString()==null||editPhone.length()==0)
                {
                    try {
                        phone1 = userinfo.get("Phone1");
                    }catch (Exception e)
                    {
                        phone1 = "9999999999";
                    }
                }
                else {
                    phone1 =editPhone.getText().toString();
                }

                User NewUser  = new User( mail,  name, password , productListed ,phone1,pic_id);

                mUserRef.child(email).setValue(NewUser);

                editName.setText("");
                editPhone.setText("");
                editPassword.setText("");
                Toast.makeText(getActivity(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                d.dismiss();
                    progressDialog.dismiss();
                }
                catch (Exception e)
                {
                    Toast.makeText(getActivity(), "Unable to get database", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                            }
                        }, 2000);
            }

        });
        //SHOW
        d.show();
    }
    public void refreshDatabase() {

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Database = dataSnapshot.getValue(Map.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mlenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Pic_Count = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }


}