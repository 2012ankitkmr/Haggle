package ankit.barter.haggle.FragmentClasses;

/**
 * Created by Ankit on 17-06-2016.
 */
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import android.widget.ListView;

import ankit.barter.haggle.ClientClasses.PicassoClient;
import ankit.barter.haggle.NoNetworkActivity;
import ankit.barter.haggle.R;
import ankit.barter.haggle.StructureClasses.Feeds;
import ankit.barter.haggle.ThreadClass;
import ankit.barter.haggle.StructureClasses.User;
import ankit.barter.haggle.Utility;

public class ProfileFragment extends Fragment {

    final static String USER_URL="https://haggle-64ac4.firebaseio.com/Users";
    final static String NOTIFICATIONS_URL="https://haggle-64ac4.firebaseio.com/Notifications";
    final static String PRODUCTDB_URL="https://haggle-64ac4.firebaseio.com/ProductIds";
    final static String Cloud_URL1="http://res.cloudinary.com/dstrkdluw/image/upload/bo_5px_solid_rgb:ffc10730,c_thumb,w_260,h_210/";
    Firebase mUserRef,mNotRef,mProductInfoRef;


    Map<String, Map<String, String>> Database = null;
    Map<String,Map<String,String>>NotificationData =null;
    Map<String, Map<String, String>> ProductDatabase = null;



    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "EmailKey";
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

        final String mail = sharedpreferences.getString(Email, "abc@gmail_com");

if(isNetworkAvailable()==false) {
    Intent in  = new Intent(getActivity(),NoNetworkActivity.class);
    startActivity(in);
}

        mUserRef = new Firebase(USER_URL);
        mNotRef = new Firebase(NOTIFICATIONS_URL);
        mProductInfoRef = new Firebase(PRODUCTDB_URL);

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

//------------------------------------------------------------------------------------------

        ListView notiflis = (ListView)rootView.findViewById(R.id.Notification_list);
       notiflis.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               String notification = parent.getItemAtPosition(position).toString();
               Map<String, String> Notif = NotificationData.get(mail);
               String valofnot = Notif.get(notification);
               String clientProductId = valofnot.substring(11);
               String type = valofnot.substring(9,10);
               String myProductId =valofnot.substring(0,8);
               if(type.equals("1"))
               {
                   showAcceptdialog(clientProductId,myProductId,1,notification);
               }
               else if(type.equals("3")) {

                   showAcceptdialog(clientProductId,myProductId,3,notification);
               }
           }
       });


        //--------------------------------------------------------------------------------------------



        return rootView;
    }

// -- ---------------------------Image Upload---------------------------------------------------------------------------



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
                            new ThreadClass("Mythread",getInputStream(image),ret);
                            progressDialog.dismiss();
                        profimageView.setImageBitmap(image);
                    }
                }, 3000);
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
                            new ThreadClass("Mythread",getInputStream(image),ret);
                            progressDialog.dismiss();
                        profimageView.setImageBitmap(image);
                    }
                }, 3000);
    }

    private String getPicId()
    {
        String email = sharedpreferences.getString(Email,"abc@gmail.com");
        String pic_Id = email;

        return pic_Id;
    }
//-------------------------------------------------------------------------------------------------------------------------------



 //--------------------------------------------notification handle------------------------------------------------------------------------------------------
public void showAcceptdialog(final String clientId, final String MyProductId, int type, final String myNot) {
    Log.e("Error at  ",clientId);

    final String mail = sharedpreferences.getString(Email, "abc@gmail_com");
    String HighlightedProduct = clientId;

    final Dialog d1 = new Dialog(getActivity());
    d1.setContentView(R.layout.card_show_wizard_client);
    final Map<String, String> currentProduct = ProductDatabase.get(HighlightedProduct);
    //      Initialisation
    ImageView img1 = (ImageView) d1.findViewById(R.id.pp1);
    ImageView img2 = (ImageView) d1.findViewById(R.id.pp2);
    ImageView img3 = (ImageView) d1.findViewById(R.id.pp3);
    TextView personname = (TextView) d1.findViewById(R.id.card_show_username);
    TextView productname = (TextView) d1.findViewById(R.id.card_show_prodname);
    TextView productnametitle = (TextView) d1.findViewById(R.id.ProductInfo);
    TextView myproduct = (TextView)d1.findViewById(R.id.Exchange_my);
    TextView location = (TextView) d1.findViewById(R.id.card_show_location);
    TextView productcategory = (TextView) d1.findViewById(R.id.card_show_prodcategory);
    TextView dateoflisting = (TextView) d1.findViewById(R.id.card_show_doA);
    TextView comments = (TextView) d1.findViewById(R.id.Comments);

    // Assignment --------------------

    PicassoClient.downloadImage(d1.getContext(), Cloud_URL1 + currentProduct.get("Product_pic1"), img1);
    PicassoClient.downloadImage(d1.getContext(), Cloud_URL1 + currentProduct.get("Product_pic2"), img2);
    PicassoClient.downloadImage(d1.getContext(), Cloud_URL1 + currentProduct.get("Product_pic3"), img3);
    personname.setText(currentProduct.get("PersonName"));
    productname.setText(currentProduct.get("Name"));
    productnametitle.setText(currentProduct.get("Name"));
    location.setText(currentProduct.get("Location"));
    productcategory.setText(currentProduct.get("Product_Category"));
    dateoflisting.setText(currentProduct.get("Date_Of_Listing"));
    comments.setText(currentProduct.get("Comments"));
    myproduct.setText(ProductDatabase.get(MyProductId).get("Name"));


    TextView t = (TextView)d1.findViewById(R.id.clientDetails);
    t.setText("");
    final Button AcceptButton = (Button) d1.findViewById(R.id.btn_accept);
   final Button DenyButton = (Button) d1.findViewById(R.id.btn_deny);

    if(type==1) {

        DenyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Denying...");
                progressDialog.show();


                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                TextView t = (TextView) d1.findViewById(R.id.clientDetails);

                                t.setText("The client's phone number is '" + Database.get(currentProduct.get("PersonId").replace(".", "_")).get("Phone1") + "' you can contact him personally");

                                String notification = null;
                                notification = Database.get(mail).get("Name") + " has Denied your request for the product : '" +
                                        ProductDatabase.get(MyProductId).get("Name")
                                        + "' in Exchange with your product '" + currentProduct.get("Name") + "' Check out other Products in Feeds" ;

                                mNotRef.child(currentProduct.get("PersonId").replace(".", "_")).child(notification).setValue(MyProductId + "_2_" + clientId);
                                mNotRef.child(mail).child(myNot).setValue(null);

                                progressDialog.dismiss();
                                d1.dismiss();
                            }
                        }, 2000);

            }

        });


        AcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Contacting...");
                progressDialog.show();


                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                TextView t = (TextView) d1.findViewById(R.id.clientDetails);

                                t.setText("The client's phone number is '" + Database.get(currentProduct.get("PersonId").replace(".", "_")).get("Phone1") + "' you can contact him personally");
AcceptButton.setVisibility(View.GONE);
                                DenyButton.setVisibility(View.GONE);

                                String notification = null;
                                notification = Database.get(mail).get("Name") + " has approved your request for the product : '" +
                                        ProductDatabase.get(MyProductId).get("Name")
                                        + "' in Exchange with your product '" + currentProduct.get("Name") + "' You can contact him at '" + Database.get(mail).get("Phone1") + "' ";

                                mNotRef.child(currentProduct.get("PersonId").replace(".", "_")).child(notification).setValue(MyProductId + "_2_" + clientId);
                                mNotRef.child(mail).child(myNot).setValue(clientId + "_3_" + MyProductId);

                                progressDialog.dismiss();

                            }
                        }, 2000);

            }

        });
    }
    else {
        t.setText("The client's phone number is '" + Database.get(currentProduct.get("PersonId").replace(".", "_")).get("Phone1") + "' you can contact him personally");

   AcceptButton.setVisibility(View.GONE);
        DenyButton.setVisibility(View.GONE);
    }
    //SHOW
    d1.show();

}



  //-----------------------------------------------------------------------------------------------------------------------------------

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

                User NewUser  = new User( mail,  name, password , productListed ,phone1,pic_id,mail);

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

        mProductInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProductDatabase = dataSnapshot.getValue(Map.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Database = dataSnapshot.getValue(Map.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mNotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              NotificationData   = dataSnapshot.getValue(Map.class);
                updateNotifications(NotificationData);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

//-------------------------------------------------------------------------------------------------

  //-----------------------------Updating Notifications----------------------------------------------

    public void updateNotifications(Map<String,Map<String,String>>MyNotifications)
    {

        String mail =sharedpreferences.getString(Email,"abc@gmail_com");
try {
    Map<String, String> Notifs = MyNotifications.get(mail);
    ArrayList<String> notArray = new ArrayList<>(Notifs.keySet());

    ListView listView = (ListView) getActivity().findViewById(R.id.Notification_list);
    ArrayAdapter<String> Adapter;
    Adapter = new ArrayAdapter<String>(getActivity(), R.layout.mynotlist, notArray);
    listView.setAdapter(Adapter);
    listView.setEmptyView(getActivity().findViewById(R.id.empty));
}catch (Exception e)
{
    Log.e("No Notification","Profile");
}
    }


    //----------------------------------------------------------------------------------------------



    public boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
        Log.e("Network Testing", "Available");
        return true;
    }

    Log.e("Network Testing", "Not Available");
    return false;
}

}