package ankit.barter.haggle.FragmentClasses;

/**
 * Created by Ankit on 17-06-2016.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Spinner;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ankit.barter.haggle.CardAdapterPackages.FeedAdapter;
import ankit.barter.haggle.Categories;
import ankit.barter.haggle.ClientClasses.PicassoClient;
import ankit.barter.haggle.NoNetworkActivity;
import ankit.barter.haggle.R;
import ankit.barter.haggle.RecyclerItemClickListener;
import ankit.barter.haggle.StructureClasses.Feeds;


public class FeedFragment extends Fragment {

    final static String CATEGORY_LIST_URL ="https://haggle-64ac4.firebaseio.com/Categorylist";
    final static String PRODUCT_LIST_URL ="https://haggle-64ac4.firebaseio.com/ProductsListed";
    final static String PRODUCTDB_URL="https://haggle-64ac4.firebaseio.com/ProductIds";
    final static String USER_URL="https://haggle-64ac4.firebaseio.com/Users";
    final static String ROOT_URL="https://haggle-64ac4.firebaseio.com";
    final static String Cloud_URL="http://res.cloudinary.com/dstrkdluw/image/upload/g_face,c_thumb,w_250,h_250/";
    final static String Cloud_URL1="http://res.cloudinary.com/dstrkdluw/image/upload/bo_5px_solid_rgb:ffc10730,c_thumb,w_260,h_210/";

    RecyclerView rv;
    Firebase mProductInfoRef,mProductListRef,mCategorylistRef,mUserRef,mRootRef;

    ArrayList<Feeds> feeds=new ArrayList<>();
    FeedAdapter adapter;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "EmailKey";
    SharedPreferences sharedpreferences;


    TreeMap<String, Map<String, String>> ProductDatabase = null;
    TreeMap<String,Map<String,String>> ProductListDatabase = null;
    Map<String,Map<String,String>> CategoryData = null;
    Map<String,Map<String,String>>UserDataBase =null;
    Map<String,TreeMap<String,String>>Database =null;


    Map<String,Integer> CategoryMap = new HashMap<>();

    public FeedFragment() {

        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        rv= (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);

        progressDialog= new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching Data...");

        progressDialog.setCancelable(false);
        progressDialog.show();


        rv.setLayoutManager(new LinearLayoutManager(getActivity()));


        if(isNetworkAvailable()==false) {

            Intent in  = new Intent(getActivity(),NoNetworkActivity.class);
            startActivity(in);

        }

        mProductInfoRef=new Firebase(PRODUCTDB_URL);
        mProductListRef = new Firebase(PRODUCT_LIST_URL);
        mCategorylistRef = new Firebase(CATEGORY_LIST_URL);
        mUserRef  = new Firebase(USER_URL);
        mRootRef = new Firebase(ROOT_URL);

        sharedpreferences = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        for(String val: Categories.CATEGORIES)
        {
            CategoryMap.put(val,1);
        }


        refreshData();



        updateProductlists();
        rv.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        ShowDialog(position);
                        //Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                        // do whatever
                    }
                })
        );

        FloatingActionButton filterfab = (FloatingActionButton)rootView.findViewById(R.id.fab_filter);
        filterfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // rv.scrollToPosition(0);
                showFilterDialog();
            }
        });


        return rootView;
    }

    private List<String> SelectCategorylist = new ArrayList<>();



    public void showFilterDialog()
    {

        final Dialog d=new Dialog(getActivity());
        d.setContentView(R.layout.filterdialog);
        final ListView multiple_select = (ListView)d.findViewById(R.id.Categories_show_list_filter);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, Categories.CATEGORIES);
        multiple_select.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        multiple_select.setAdapter(adapter);


        for(String val: Categories.CATEGORIES)
        {
            CategoryMap.put(val,0);
        }


        Button filterbtn = (Button)d.findViewById(R.id.btn_filter);
        filterbtn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {

                // String selected = "";
                int cntChoice = multiple_select.getCount();
                SelectCategorylist.clear();
                SparseBooleanArray sparseBooleanArray = multiple_select.getCheckedItemPositions();
                for(int i = 0; i < cntChoice; i++){
                    if(sparseBooleanArray.get(i)) {
//                        selected += multiple_select.getItemAtPosition(i).toString() + "\n";
                        try {
                            SelectCategorylist.add(multiple_select.getItemAtPosition(i).toString());

                        }catch (Exception e)
                        {
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }


                for(String val:SelectCategorylist)
                {
                    CategoryMap.put(val,1);
                }
                Toast.makeText(getActivity(), "Categories Selected!", Toast.LENGTH_SHORT).show();
                ProductDatabase.clear();
                refreshData();
                d.dismiss();
            }});

        d.show();

    }


    ArrayList<String> MyProducts= null,MyProductId =null;

    String clientSelectedProduct=null,clientSelectedProductId =null;
    String HighlightedProduct = null;
    public void ShowDialog(int position)
    {


        final String mail = sharedpreferences.getString(Email, "abc@gmail_com");
        int cnt = 0;


        for(String keys : ProductDatabase.keySet()) {
            Map<String, String> mymap = ProductDatabase.get(keys);
            Log.e("Keys",keys);
            if (mail.equals(mymap.get("PersonId").replace(".", "_"))||CategoryMap.get(mymap.get("Product_Category"))==0)
                continue;
            if(cnt==position)
            {
                HighlightedProduct = keys;
                break;
            }
            cnt++;
        }

        //Log.e("Highlighted",HighlightedProduct);

        final Dialog d1=new Dialog(getActivity());
        d1.setContentView(R.layout.card_show_wizard);

        final Map<String,String> currentProduct = ProductDatabase.get(HighlightedProduct);
        //      Initialisation
        ImageView img1 = (ImageView)d1.findViewById(R.id.pp1);
        ImageView img2 = (ImageView)d1.findViewById(R.id.pp2);
        ImageView img3 = (ImageView)d1.findViewById(R.id.pp3);
        TextView personname =(TextView)d1.findViewById(R.id.card_show_username);
        TextView productname =(TextView)d1.findViewById(R.id.card_show_prodname);
        TextView productnametitle =(TextView)d1.findViewById(R.id.ProductInfo);
        TextView location =(TextView)d1.findViewById(R.id.card_show_location);
        TextView productcategory =(TextView)d1.findViewById(R.id.card_show_prodcategory);
        TextView dateoflisting =(TextView)d1.findViewById(R.id.card_show_doA);
        TextView comments =(TextView)d1.findViewById(R.id.Comments);
        ListView listView = (ListView)d1.findViewById(R.id.Categories_show);
        Spinner spinner = (Spinner)d1.findViewById(R.id.myproducts_spinner);

        // Assignment --------------------

        //Log.e("Pic1",Cloud_URL+mymap.get("Product_pic1"));
        PicassoClient.downloadImage(d1.getContext(),Cloud_URL1+currentProduct.get("Product_pic1"),img1);
        PicassoClient.downloadImage(d1.getContext(),Cloud_URL1+currentProduct.get("Product_pic2"),img2);
        PicassoClient.downloadImage(d1.getContext(),Cloud_URL1+currentProduct.get("Product_pic3"),img3);
        personname.setText(currentProduct.get("PersonName"));
        productname.setText(currentProduct.get("Name"));
        productnametitle.setText(currentProduct.get("Name"));
        location.setText(currentProduct.get("Location"));
        productcategory.setText(currentProduct.get("Product_Category"));
        dateoflisting.setText(currentProduct.get("Date_Of_Listing"));
        comments.setText(currentProduct.get("Comments"));


        Map<String,String>prodCategory = CategoryData.get(HighlightedProduct);
        ArrayList<String>prodCategoryarray = new ArrayList<>(prodCategory.keySet());

        ArrayAdapter<String> Adapter;
        Adapter = new ArrayAdapter<String>(getActivity(),R.layout.mylist,prodCategoryarray);
        listView.setAdapter(Adapter);


        ArrayAdapter<String> myproductAdapter = null;
        if(ProductListDatabase==null)
        Log.e("Mail ",mail);

         final Map<String,String> myData = ProductListDatabase.get(mail);

        try {

            MyProducts = new ArrayList<>();
            MyProductId = new ArrayList<>();
        for(String val :myData.keySet())
        {
        MyProducts.add(ProductDatabase.get(val).get("Name"));
            MyProductId.add(val);
        }
            myproductAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,MyProducts);
        }catch (Exception e)
        {
            Log.e("MyProducts ","null");
            MyProducts = null;
        }

        spinner.setAdapter(myproductAdapter);
//        final int Number = -1 ;



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                clientSelectedProduct = parentView.getItemAtPosition(position).toString();
                clientSelectedProductId = MyProductId.get(position);
                // your code here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        ImageView InterestButton = (ImageView) d1.findViewById(R.id.Interested);


        InterestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyProducts == null) {
                    Toast.makeText(getActivity(), "Please Add A Product In your Profile! ", Toast.LENGTH_LONG).show();
                }
                else if(clientSelectedProduct==null)
                {
                    Toast.makeText(getActivity(), "Select Your Product to Replace With! ", Toast.LENGTH_LONG).show();
                }
                else {

                    final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Contacting...");
                    progressDialog.show();
                    progressDialog.setCancelable(false);


                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    String notification = null;
                                    notification = UserDataBase.get(mail).get("Name")+" has shown interest in your Product : '"+currentProduct.get("Name")
                                            +"' in Exchange with his product '"+clientSelectedProduct+"' Click to Respond ";

                                    mRootRef.child("Notifications").child(currentProduct.get("PersonId").replace(".","_")).child(notification).setValue(HighlightedProduct+"_1_"+clientSelectedProductId);

                                    Toast.makeText(getActivity(), "You shall recieve notification once the person accepts the request!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    d1.dismiss();
                                }
                            }, 2000);
                }
            }

        });
        //SHOW
        d1.show();
    }

    public void updateProductlists()
    {
        mCategorylistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CategoryData = dataSnapshot.getValue(Map.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mProductListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProductListDatabase = dataSnapshot.getValue(TreeMap.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }


    //-------------------------RERESHING CARDS-----------------------------------------------------------------------


    public void refreshData()
    {
        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Database = dataSnapshot.getValue(Map.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDataBase = dataSnapshot.getValue(Map.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mProductInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProductDatabase = dataSnapshot.getValue(TreeMap.class);
                getUpdates(ProductDatabase);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getUpdates(TreeMap<String,Map<String,String>>myfeed)
    {
        feeds.clear();
        String mail = sharedpreferences.getString(Email, "abc@gmail_com");
        for(String keys : myfeed.keySet())
        {
            Map<String,String>cardProduct = myfeed.get(keys);
            if(mail.equals(cardProduct.get("PersonId").replace(".","_"))||CategoryMap.get(cardProduct.get("Product_Category"))==0)
                continue;

            Feeds f=new Feeds();
            f.setDoa(cardProduct.get("Date_Of_Listing"));
            f.setName(cardProduct.get("Name"));
            f.setUrl(Cloud_URL+cardProduct.get("Pic_id"));
            f.setLocation(cardProduct.get("Location"));
            f.setCategory(cardProduct.get("Product_Category"));
            f.setPersonName(cardProduct.get("PersonName"));
            feeds.add(f);
        }
            adapter = new FeedAdapter(getContext(),feeds);
            rv.setAdapter(adapter);

            try {
                RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.myfeedlayout);
                rl.setBackgroundResource(R.drawable.blurbackground);
            }catch (Exception e)
            {
                Log.e(e.toString(),"Problem in Background setting");
            }

        progressDialog.dismiss();

        if (feeds.size() > 0) {
            Log.e("Here","size>0");
        }
       else {
            Snackbar.make(getView(),"No Results to Display",Snackbar.LENGTH_LONG).show();
            Log.e("Here","size==0");
        }
    }


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
//---------------------------------------------------------------------------------------------------------------------