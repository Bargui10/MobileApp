package com.example.e_commerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_commerce.Model.Products;
import com.example.e_commerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {
    private Button addToCartBtn, addButton, subButton;
    private ImageView productImage;
    private TextView productPrice, productDescription, productName, quantityTextView;
    private String productID = "",state = "Normal";
    private Integer quantity = 1;

    @Override
    protected void onStart() {
        super.onStart();
        checkOrderStatus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        addToCartBtn = findViewById(R.id.pd_add_to_card_button);
        productImage = findViewById(R.id.product_image_details);
        productName = findViewById(R.id.product_name_details);
        productDescription = findViewById(R.id.product_description_details);
        productPrice = findViewById(R.id.product_price_details);
        quantityTextView = findViewById(R.id.qtr);
        addButton = findViewById(R.id.add);
        subButton = findViewById(R.id.sub);

        productID = getIntent().getStringExtra("pid");

        getProductDetails(productID);

        quantityTextView.setText(String.valueOf(quantity));

        addButton.setOnClickListener(v -> {
            if (quantity < 10) { // Maximum quantity is 10
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
                // Optional: Update total price or other logic here
            }
        });

        subButton.setOnClickListener(v -> {
            if (quantity > 1) { // Minimum quantity is 1
                quantity--;
                quantityTextView.setText(String.valueOf(quantity));
                // Optional: Update total price or other logic here
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addingToCartList();
                if(state.equals("Order Placed") || (state.equals("Order Shipped"))){
                    Toast.makeText(ProductDetailsActivity.this, "You can buy More Products,once you recieve Your Order", Toast.LENGTH_LONG).show();
                }else {
                    addingToCartList();
                }
            }
        });
    }

    private void addingToCartList() {
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("pid", productID);
        cartMap.put("pname", productName.getText().toString());
        cartMap.put("price", productPrice.getText().toString());
        cartMap.put("date", saveCurrentDate);
        cartMap.put("time", saveCurrentTime);
        cartMap.put("quantity", quantity.toString()); // Convert quantity to String
        cartMap.put("discount", "");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products")
                .child(productID)
                .updateChildren(cartMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone()).child("Products")
                                    .child(productID)
                                    .updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(ProductDetailsActivity.this,"Added To Your Cart List.",Toast.LENGTH_SHORT);

                                                Intent intent = new Intent(ProductDetailsActivity.this,HomeActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void getProductDetails(String productID) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        productsRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Products products = snapshot.getValue(Products.class);
                    productName.setText(products.getPname());
                    productPrice.setText(products.getPrice());
                    productDescription.setText(products.getDescription());
                    Picasso.get().load(products.getImage()).into(productImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkOrderStatus(){
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference()
                .child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    String shippingState = snapshot.child("state").getValue().toString();

                    if(shippingState.equals("shipped")){
                        state = "Order Shipped";

                    } else if (shippingState.equals("not shipped")){
                        state = "Order Placed";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}