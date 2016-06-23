package ankit.barter.haggle;

/**
 * Created by Ankit on 17-06-2016.
 */
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ImageView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;;import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ankit.barter.haggle.AdapterPackages.FeedAdapter;
import ankit.barter.haggle.StructureClasses.Feeds;


public class FeedFragment extends Fragment {


    final static String DB_URL="https://haggle-64ac4.firebaseio.com/ProductIds";
    final static String Cloud_URL="http://res.cloudinary.com/dstrkdluw/image/upload/g_face,c_thumb,w_200,h_200/";
    RecyclerView rv;
    Firebase fire;
    ArrayList<Feeds> feeds=new ArrayList<>();
    FeedAdapter adapter;

    TreeMap<String, Map<String, String>> Database = null;



    public FeedFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

         rv= (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);

        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        fire=new Firebase(DB_URL);

        refreshData();

        return rootView;

    }

    public void refreshData()
    {
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
    }

    private void getUpdates(TreeMap<String,Map<String,String>>myfeed)
    {
        //Log.e("Reached","Getting updates");

        feeds.clear();
        for(String keys : myfeed.keySet())
        {
            Map<String,String>mymap = myfeed.get(keys);
            Feeds f=new Feeds();
            f.setName(mymap.get("Name"));
            //Toast.makeText(getActivity(), Cloud_URL+mymap.get("Pic_id"), Toast.LENGTH_SHORT).show();
            f.setUrl(Cloud_URL+mymap.get("Pic_id")+".png");
            f.setLocation(mymap.get("Location"));
            f.setCategory(mymap.get("Product Category"));
            f.setPersonName(mymap.get("PersonName"));
            feeds.add(f);
        }
        if(feeds.size()>0)
        {
            adapter = new FeedAdapter(getContext(),feeds);
            rv.setAdapter(adapter);
        }else {
            Toast.makeText(getActivity(),"No data",Toast.LENGTH_SHORT).show();
        }
    }
}