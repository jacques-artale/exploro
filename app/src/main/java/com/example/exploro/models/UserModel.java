package com.example.exploro.models;

import android.app.Activity;
import android.util.Log;

import com.example.exploro.models.schemas.Trips;
import com.example.exploro.models.schemas.User;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class UserModel {
    private DatabaseReference db;
    private Activity activity;


    public interface ResultStatus {
        void resultLoaded(List<User> users);
    }
    public interface TripsResultStatus {
        void tripsResultLoaded(List<Trips> trips);
    }

    public UserModel(Activity activity){
        db = FirebaseDatabase.getInstance().getReference();
        //addUser("Dennis", "test", "dennis@gmail.com","898979");
    }
    public UserModel(){
        db = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Authenticate user against database
     *
     * @param username - input username
     * @param password - input password
     * @param result - Return correctly authenticated users
     */
    public void authUser(String username, String password, final ResultStatus result) {
        db.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            List<User> users = new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                // If database error
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    result.resultLoaded(users);
                    return;
                }

                // If no user exist with that username then return
                if (task.getResult().getValue() == null) {
                    result.resultLoaded(users);
                    return;
                }

                // Authenticate password
                String aUsername = task.getResult().child("username").getValue().toString();
                String aPassword = task.getResult().child("password").getValue().toString();
                Base64.Decoder decoder = Base64.getDecoder();
                aPassword = new String(decoder.decode(aPassword));


                if (password.equals(aPassword))
                    users.add(task.getResult().getValue(User.class));
                result.resultLoaded(users);
            }
        });
    }

    /**
     * Create a new user in database
     *
     * @param name - full name of user
     * @param username - username
     * @param email - email
     * @param password - password
     * @return
     */

    public User createNewUser(String name, String username, String email, String password) {
        // Encode password
        String encodePassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));

        User user = new User(name, email, username, encodePassword, "8989898");
        user.experience = 0;
        // TODO: Write new user to database
        db.child("users").child(username).setValue(user);

        return user;
    }

    /**
     * Check for username in database
     *
     * @param username - Username to look for
     * @return found users in ResultStatus
     */
    public void findUsernameInUsers(String username, final ResultStatus result) {
        db.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            List<User> users = new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    return;
                }
                Log.d("firebase", String.valueOf(task.getResult().child("name").getValue()));
                users.add(task.getResult().getValue(User.class));
                result.resultLoaded(users);
                return;
            }
        });
    }

    /**
     * Check for email in database
     *
     * @param email - email to look for
     * @return found users in ResultStatus
     */
    public void findEmailInUsers(String email, final ResultStatus result) {

        db.child("users").child(email).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            List<User> users = new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    return;
                }
                Log.d("firebase", String.valueOf(task.getResult().child("email").getValue()));
                users.add(task.getResult().getValue(User.class));
                result.resultLoaded(users);
                return;
            }
        });
    }
    public void getAllUserObjects(final ResultStatus result) {

        db.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<User> users = new ArrayList<>();
                    for (DataSnapshot children : snapshot.getChildren()) {
                        User user = children.getValue(User.class);
                        users.add(user);
                    }
                    result.resultLoaded(users);
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException());
                return;

            }
        });
    }

    public void getFriendsUserObjects(String username, final ResultStatus result) {

        db.child("users").child(username).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot children : snapshot.getChildren()) {
                        User user = children.getValue(User.class);
                        users.add(user);
                    }
                    result.resultLoaded(users);
                    return;
                } else{
                    result.resultLoaded(users);
                    return;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException());
                return;

            }
        });
    }
    public void addNewFriend(User user, User friend) {
        User friendObj = new User();
        friendObj.experience = friend.experience;
        friendObj.username = friend.username;
        User userObj = new User();
        userObj.experience = user.experience;
        userObj.username = user.username;

        db.child("users").child(user.username).child("friends").child(friend.username).setValue(friendObj);
        db.child("users").child(friend.username).child("friends").child(user.username).setValue(userObj);

    }

    public void deleteFriend(String username, String friendUsername) {
        db.child("users").child(username).child("friends").child(friendUsername).removeValue();
        db.child("users").child(friendUsername).child("friends").child(username).removeValue();

    }
    public void changeEmail(String username, String newEmail) {
        db.child("users").child(username).child("email").setValue(newEmail);
    }
    public void changePassword(String username, String password) {
        password = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        db.child("users").child(username).child("password").setValue(password);
    }
    public void changeUsername(String username, String newUsername) {
        db.child("users").child(username).child("password").setValue(newUsername);
    }
    public void addUserExperience(String username, int experience) {
        db.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user.experience += experience;
                Map<String, Object> updatedValues = new HashMap<>();
                updatedValues.put("experience", user.experience);
                db.child("users").child(user.username).updateChildren(updatedValues);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", "couldn't connect to db");
            }
        });
    }
    public void addTrip(String username, String place){
        System.out.println(username + place);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        Trips trips = new Trips();
        trips.date = dateFormat.format(currentDate);
        trips.place = place;
        db.child("users").child(username).child("trips").child(place).setValue(trips);
    }
    public void addRoute(String username, ArrayList<Trips> route){
        String id = db.child("users").child(username).child("routes").push().getKey();

        for (Trips trip : route){
            db.child("users").child(username).child("routes").child(id).child(trip.place).setValue(trip);
        }


    }
    public void getUserTrips(String username, final TripsResultStatus result) {

        db.child("users").child(username).child("trips").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Trips> trips = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot children : snapshot.getChildren()) {
                        Trips trip = children.getValue(Trips.class);
                        trips.add(trip);
                        System.out.println(trip.city);
                    }
                    result.tripsResultLoaded(trips);
                    return;
                } else{
                    result.tripsResultLoaded(trips);
                    return;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException());
                return;

            }
        });
    }


}
