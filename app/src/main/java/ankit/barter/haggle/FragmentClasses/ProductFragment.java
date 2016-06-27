package ankit.barter.haggle.FragmentClasses;

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

import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.analytics.ecommerce.Product;

import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ankit.barter.haggle.CardAdapterPackages.FeedAdapter;
import ankit.barter.haggle.Categories;
import ankit.barter.haggle.ClientClasses.PicassoClient;
import ankit.barter.haggle.R;
import ankit.barter.haggle.RecyclerItemClickListener;
import ankit.barter.haggle.StructureClasses.Feeds;
import ankit.barter.haggle.StructureClasses.ProductClass;
import ankit.barter.haggle.StructureClasses.User;
import ankit.barter.haggle.ThreadClass;
import ankit.barter.haggle.Utility;


public class ProductFragment extends Fragment {


    final static String CATEGORY_LIST_URL ="https://haggle-64ac4.firebaseio.com/Categorylist";
    final static String PRODUCT_LIST_URL ="https://haggle-64ac4.firebaseio.com/ProductsListed";
    final static String USERURL="https://haggle-64ac4.firebaseio.com/Users";
    final static String CNTURL="https://haggle-64ac4.firebaseio.com/Productcnt";
    final static String DB_URL="https://haggle-64ac4.firebaseio.com/ProductIds";
    final static String Cloud_URL="http://res.cloudinary.com/dstrkdluw/image/upload/g_face,c_thumb,w_250,h_250/";
    final static String Cloud_URL1="http://res.cloudinary.com/dstrkdluw/image/upload/c_thumb,w_150,h_120/";

    RecyclerView rv;

    Firebase fire,mProductRef,mCategoryRef,mUserRef,mCntRef;

    ArrayList<Feeds> feeds=new ArrayList<>();
    FeedAdapter adapter;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "EmailKey";
    SharedPreferences sharedpreferences;


    TreeMap<String, Map<String, String>> Database = null;
    TreeMap<String,Map<String,String>> ProductData = null;
    Map<String,Map<String,String>> CategoryData = null;
    Map<String,Map<String,String>> UserDatabase = null;
    String cnt=null;


    private String userChoosenTask;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    private List<String> Categorylist = new ArrayList<>();

    String myCategory =null;

    public ProductFragment() {

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
        View rootView = inflater.inflate(R.layout.fragment_product, container, false);

        rv = (RecyclerView) rootView.findViewById(R.id.product_recycler_view);

        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        fire=new Firebase(DB_URL);
        mProductRef = new Firebase(PRODUCT_LIST_URL);
        mCategoryRef = new Firebase(CATEGORY_LIST_URL);
        mUserRef = new Firebase(USERURL);
        mCntRef = new Firebase(CNTURL);

        sharedpreferences = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        refreshData();
        updateCategorylists();
        updateProductlists();

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayaddDialog();
            }
        });


        rv.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        ShowDialog(position);
                    }
                })
        );


        return rootView;
    }
    boolean flag1= false,flag2= false,flag3= false;
    int count = 0;

    String img_id=null;
    public void displayaddDialog()
    {
        Button addBtn;
        final String mail = sharedpreferences.getString(Email, "abc@gmail.com");


        final Dialog d=new Dialog(getActivity());
        d.setContentView(R.layout.addproduct_wizard);

        final EditText addName,addLocation,addComment;
        final ImageView img1,img2,img3;
        Spinner spinner = (Spinner)d.findViewById(R.id.add_category_spinner);

        addName = (EditText) d.findViewById(R.id.add_name);
        addLocation = (EditText) d.findViewById(R.id.add_product_location);
        addComment = (EditText) d.findViewById(R.id.add_product_comment);
        img1  =(ImageView)d.findViewById(R.id.add_pp1);
         img2  =(ImageView)d.findViewById(R.id.add_pp2);
         img3  =(ImageView)d.findViewById(R.id.add_pp3);
        img1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                img_id ="1";
                flag1 =true;
                selectImage();
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag2 =true;
                img_id ="2";
                selectImage();
            }
        });
        img3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag3 =true;
                img_id ="3";
                selectImage();
            }
        });



        final ListView multiple_select = (ListView)d.findViewById(R.id.add_category_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, Categories.CATEGORIES);
        multiple_select.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        multiple_select.setAdapter(adapter);
        final Button getChoice = (Button)d.findViewById(R.id.choicefill);


        getChoice.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {

               // String selected = "";
                int cntChoice = multiple_select.getCount();
Categorylist.clear();
                SparseBooleanArray sparseBooleanArray = multiple_select.getCheckedItemPositions();
                for(int i = 0; i < cntChoice; i++){
                    if(sparseBooleanArray.get(i)) {
//                        selected += multiple_select.getItemAtPosition(i).toString() + "\n";
                        try {
                            Categorylist.add(multiple_select.getItemAtPosition(i).toString());

                        }catch (Exception e)
                        {
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                Toast.makeText(getActivity(), "Categories Selected!", Toast.LENGTH_SHORT).show();

            }});



        ArrayAdapter<String> Adapter;
        Adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line, Categories.CATEGORIES);
        spinner.setAdapter(Adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                myCategory = parentView.getItemAtPosition(position).toString();
                                // your code here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });



        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
       final String DateofList = df.format(c.getTime());

        Map<String,String>UserData = UserDatabase.get(mail);
        final String PersonName =UserData.get("Name");
        final String PersonId = UserData.get("Email");
        final String Pic_id = UserData.get("Pic_id");
        final String Product_id = cnt;
        final String Product_pic1 = cnt+"_"+"1";
        final String Product_pic2 = cnt+"_"+"2";
        final String Product_pic3 = cnt+"_"+"3";

        addBtn = (Button) d.findViewById(R.id.add_a_newproduct);

        addBtn.setOnClickListener(new View.OnClickListener() {
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
                                    if(flag1==false||flag2==false||flag3==false||count<3)
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Please Upload Three Images", Toast.LENGTH_SHORT).show();

                                    }
                                    else {
                                    if(Categorylist==null)
                                    {progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Please Select Categories of Interest", Toast.LENGTH_SHORT).show();

                                    }
                                    else if (myCategory==null)
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Please Select Category", Toast.LENGTH_SHORT).show();

                                    }
                                   else
                                    {
                                        String email = sharedpreferences.getString(Email, "abc@gmail.com");
                                        try {
                                            String name=null,location=null,comment=null;
                                            name = addName.getText().toString();
                                            location =addLocation.getText().toString();
                                            comment = addComment.getText().toString();
                                            if(name==null||location==null||comment==null)
                                            {
                                                Toast.makeText(getActivity(), "Please Select Category", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Map<String, String> catmap = new HashMap<String, String>();
                                             for (int i = 0;i<Categorylist.size();i++)
                                             {
                                                catmap.put(Categorylist.get(i).toString(), String.valueOf(i+1));
                                            }

                                                mCategoryRef.child(Product_id).setValue(catmap);


                                                mProductRef.child(email).child(Product_id).setValue(Pic_id);


                                                ProductClass productClass =new ProductClass(name,Pic_id,location,myCategory,PersonName,
                                                Product_pic1,Product_pic2,Product_pic3,Product_id,comment,DateofList,PersonId);
                                                 fire.child(Product_id).setValue(productClass);

                                                int productnum = Integer.parseInt(Product_id);
                                                mCntRef.setValue(String.valueOf(productnum + 1));

                                                Toast.makeText(getActivity(), "Product Added!", Toast.LENGTH_SHORT).show();
                                                //Fill the Database
                                                progressDialog.dismiss();
                                                d.dismiss();
                                            }

                                        } catch (Exception e) {
                                            Log.e("Error in Add",e.toString());
                                            Toast.makeText(getActivity(), "Please Fill All Fields ", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                    }

                                }
                            }, 2000);


            }

        });
        //SHOW
        d.show();
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
                        new ThreadClass("Mythread",getInputStream(image),ret);
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                        count++;

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
                        Toast.makeText(getActivity(), "Image Uploaded!", Toast.LENGTH_SHORT).show();
                        count++;

                    }
                }, 3000);
    }

    private String getPicId()
    {
        String pic_Id = cnt+"_"+img_id;
        return pic_Id;
    }


    String Usercategoryselection = null;
    public void ShowDialog(int position)
    {

        final String mail = sharedpreferences.getString(Email, "abc@gmail_com");
        int cnt = 0;
        String HighlightedProduct = null;

        for(String keys : Database.keySet()) {
            Map<String, String> mymap = Database.get(keys);
            if (mail.equals(mymap.get("PersonId").replace(".", "_"))==false)
                continue;
            if(cnt==position)
            {
                HighlightedProduct = keys;
                break;
            }
            cnt++;
        }
        final Dialog d1=new Dialog(getActivity());
        d1.setContentView(R.layout.card_update_delete_wizard);

        Map<String,String> mymap = Database.get(HighlightedProduct);
        //      Initialisation
        ImageView img1 = (ImageView)d1.findViewById(R.id.pp1);
        ImageView img2 = (ImageView)d1.findViewById(R.id.pp2);
        ImageView img3 = (ImageView)d1.findViewById(R.id.pp3);
        EditText productname =(EditText) d1.findViewById(R.id.card_show_prodname);
        EditText location =(EditText)d1.findViewById(R.id.card_show_location);
        TextView dateoflisting =(TextView)d1.findViewById(R.id.card_show_doA);
        EditText comments =(EditText) d1.findViewById(R.id.Comments);
        Spinner spinner = (Spinner)d1.findViewById(R.id.spinner_change_category);

        // Assignment --------------------

        //Log.e("Pic1",Cloud_URL+mymap.get("Product_pic1"));
        PicassoClient.downloadImage(d1.getContext(),Cloud_URL1+mymap.get("Product_pic1"),img1);
        PicassoClient.downloadImage(d1.getContext(),Cloud_URL1+mymap.get("Product_pic2"),img2);
        PicassoClient.downloadImage(d1.getContext(),Cloud_URL1+mymap.get("Product_pic3"),img3);
        productname.setText(mymap.get("Name"));
        location.setText(mymap.get("Location"));
        dateoflisting.setText(mymap.get("Date Of Listing"));
        comments.setText(mymap.get("Comments"));


        ArrayAdapter<String> myproductAdapter;
//        if(ProductData==null)
//            Log.e("Mail ",mail);

        myproductAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,Categories.CATEGORIES);
        spinner.setAdapter(myproductAdapter);
        spinner.setSelection(Arrays.asList(Categories.CATEGORIES).indexOf(mymap.get("Product_Category")));
//        final int Number = -1 ;
         Usercategoryselection = mymap.get("Product_Category");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                Usercategoryselection = parentView.getItemAtPosition(position).toString();
                // your code here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        Button Updatebuttton = (Button) d1.findViewById(R.id.update_btn);





//---------------------------------------DELETE BUTTON Functionality------------------------------------------

        Button DeleteButton = (Button) d1.findViewById(R.id.delete_btn);

        final String ToDelete = HighlightedProduct;

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Deleting...");
                    progressDialog.show();


                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {

                                    mProductRef.child(mail).child(ToDelete).setValue(null);
                                    fire.child(ToDelete).setValue(null);
                                    mCategoryRef.child(ToDelete).setValue(null);
                                    progressDialog.dismiss();
                                    d1.dismiss();
                                }
                            }, 3000);
                }


        });
        //--------------------------------------------------------------------------------------------------------------------------------
        //SHOW
        d1.show();
    }

    public void updateCategorylists() {
        mCategoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CategoryData = dataSnapshot.getValue(Map.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void updateProductlists()
    {
        mProductRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProductData = dataSnapshot.getValue(TreeMap.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }


    public void refreshData()
    {mCntRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            cnt = dataSnapshot.getValue(String.class);

        }
        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    });

        fire.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Database = dataSnapshot.getValue(TreeMap.class);
                getUpdates(Database);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDatabase = dataSnapshot.getValue(TreeMap.class);

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getUpdates(TreeMap<String,Map<String,String>>myfeed)
    {
        //Log.e("Reached","Getting updates");

     try {
         feeds.clear();

         for (String keys : myfeed.keySet()) {
             String mail = sharedpreferences.getString(Email, "abc@gmail_com");

             Map<String, String> mymap = myfeed.get(keys);
             if (mail.equals(mymap.get("PersonId").replace(".", "_"))==false)
                 continue;

             Feeds f = new Feeds();

             f.setName(mymap.get("Name"));
             f.setDoa(mymap.get("Date_Of_Listing"));
             f.setUrl(Cloud_URL + mymap.get("Pic_id"));
             f.setLocation(mymap.get("Location"));
             f.setCategory(mymap.get("Product_Category"));
             f.setPersonName(mymap.get("PersonName"));
             feeds.add(f);
         }
         if (feeds.size() > 0) {
             TextView t = (TextView) getActivity().findViewById(R.id.Blank_text);
             t.setVisibility(View.GONE);

             adapter = new FeedAdapter(getActivity(), feeds);
             rv.setAdapter(adapter);
         } else {
 //            Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
             TextView t = (TextView) getActivity().findViewById(R.id.Blank_text);
             t.setVisibility(View.VISIBLE);
         }
     }
     catch (Exception e)
     {
         Toast.makeText(getActivity(), "Add Products!", Toast.LENGTH_SHORT).show();
     }
    }
}