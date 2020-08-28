package com.example.saveme.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.saveme.category.Document;
import com.example.saveme.main.Category;
import com.example.saveme.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;


public class FirebaseMediate {
    private static Map<String, Category> categories = new HashMap<>();
    private static final String TAG = "FirebaseMediate";
    private static FirebaseFirestore db;
    private static DocumentReference userDocumentRef;
    private static CollectionReference usersCollectionRef;
    private static Context appContext;
    private static CollectionReference categoriesRef;
    private static DocumentSnapshot userDocumentSnapshot;

    /**
     * This method initializes the firestore fields from data from the database.
     *
     * @param context
     */
    public static void initializeDataFromDB(Context context) {
        appContext = context;
        db = FirebaseFirestore.getInstance();
        usersCollectionRef = db.collection("users");//todo if use only once it's ok here

        String userDocumentPath = MyPreferences.getUserDocumentPathFromPreferences(appContext);
        if (userDocumentPath != null) {
            userDocumentRef = db.document(userDocumentPath);
            initializeUserDocumentSnapshotFromDB();
            categoriesRef = userDocumentRef.collection("categories");
        }
    }

    /**
     * This method initializes the userDocumentSnapshot.
     */
    public static void initializeUserDocumentSnapshotFromDB() {
        userDocumentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                userDocumentSnapshot = value;
            }
        });
        Log.d(TAG, "successful setUserCollectionRef from db");
    }

    /**
     * This method is a getter  for user categories list.
     *
     * @return user categories list.
     */
    public static Map<String, Category> getUserCategories() {
        Log.d(TAG, "got to getUserCategories");
        User user = userDocumentSnapshot.toObject(User.class);
        categories = user.getCategories();
        return categories;
    }

    /**
     * This method adds a category to user categories list.
     *
     * @param category category to add.
     */
    public static void addCategory(final Category category) {
        userDocumentRef.update("categories", FieldValue.arrayUnion(category)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "add category %s successfully" + category.title);
                } else {
                    Log.e(TAG, "error occurred while trying to add category %s " + category.title);
                }
            }
        });
    }

    /**
     * This method removes a category from user categories list.
     *
     * @param category to be removed from user categories list.
     */
    public static void removeCategory(final Category category) {
        userDocumentRef.update("categories", FieldValue.arrayRemove(category)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "remove category %s successfully" + category.title);
                } else {
                    Log.e(TAG, "error occurred while trying to remove category %s " + category.title);
                }
            }
        });
    }

    /**
     * This method adds a user to firestore database. called only once, when sign up.
     *
     * @param userToAdd the user to add.
     */
    public static void addUserToFirestoreDB(User userToAdd) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//todo use

        // Add a new User document with a generated ID
        usersCollectionRef.add(userToAdd)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        userDocumentRef = documentReference;
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        MyPreferences.saveUserDocumentPathToPreferences(appContext, documentReference.getPath());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    /**
     * Add new document to firestore database.
     * @param CategoryName The category the document is been added to.
     * @param newDocument The new document.
     */
    public static void addNewDocument(String CategoryName, Document newDocument) {
        String name = "categories." + CategoryName + ".docsList";
        userDocumentRef.update(name, FieldValue.arrayUnion(newDocument));
    }

    /**
     *
     * @param CategoryName The category the document is been removed from.
     * @param documentToDelete The document to delete.
     */
    public static void removeDocument(String CategoryName, Document documentToDelete) {
        String name = "categories." + CategoryName + ".docsList";
        userDocumentRef.update(name, FieldValue.arrayRemove(documentToDelete));
    }

    public static void updateDocument(String CategoryName, Document documentToUpdate) {
        String name = "categories." + CategoryName + ".docsList";
        //todo check if change in name will insert a new document instead of updating old one
        userDocumentRef.update(name, FieldValue.arrayUnion(documentToUpdate));
    }

    /**
     * This method returns the default categories list.
     *
     * @return default categories list.
     */
    public static Map<String, Category> getDefaultCategories() {
        Map<String, Category> defaultCategories = new HashMap<>();
        defaultCategories.put("Car", new Category("Car", "car category"));
        defaultCategories.put("Bank", new Category("Bank", "bank category"));
        defaultCategories.put("Personal", new Category("Personal", "personal category"));
        return defaultCategories;
    }

}
