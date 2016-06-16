package ankit.barter.haggle;

import java.util.Map;

public class User  {
    public String Username;
    public String Email;
    public String Interests;
    public String Name;
    public String Password;
    public String ProductListed;
    public String Phone1;


    public User() {

        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String interests, String name, String password, String productListed, String phone1) {
        this.Username = username;
        this.Email = email;
        this.Interests = interests;
        this.Name = name;
        this.Password = password;
        this.ProductListed = productListed;
        this.Phone1 = phone1;
    }

    public User(Map<String,String> data) {
        this.Username = data.get("Username");
        this.Email = data.get("Email");
        this.Interests = data.get("Interests");
        this.Name = data.get("Name");
        this.Password = data.get("Password");
        this.ProductListed = data.get("ProductListed");
        this.Phone1 = data.get("Phone1");
    }

}