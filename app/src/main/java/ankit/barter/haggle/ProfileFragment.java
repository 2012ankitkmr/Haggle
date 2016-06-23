package ankit.barter.haggle;

/**
 * Created by Ankit on 17-06-2016.
 */
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.Map;
import java.util.TreeMap;

public class ProfileFragment extends Fragment {

    final static String USER_URL="https://haggle-64ac4.firebaseio.com/Users";
    Firebase mUserRef;

    Map<String, Map<String, String>> Database = null;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "EmailKey";
    public static final String loginKey = "loginKey";
    SharedPreferences sharedpreferences;

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

        mUserRef=new Firebase(USER_URL);



        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_edit_profile);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDialog();
            }
        });


        return rootView;
    }
    private void displayDialog()
    {
        final EditText editName,editPhone,editPassword;
        Button saveBtn;
        refreshDatabase();

        final Dialog d=new Dialog(getActivity());

        d.setTitle("Edit Profile");
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
    }


}