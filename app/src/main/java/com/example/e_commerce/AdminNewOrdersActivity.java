package com.example.e_commerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.e_commerce.Model.AdminOrders;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminNewOrdersActivity extends AppCompatActivity {
    private RecyclerView ordersList;
    private DatabaseReference orderRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);

        orderRef = FirebaseDatabase.getInstance().getReference()
                .child("Orders");
        ordersList = findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<AdminOrders> options = new
                FirebaseRecyclerOptions.Builder<AdminOrders>()
                .setQuery(orderRef, AdminOrders.class)
                .build();
        FirebaseRecyclerAdapter<AdminOrders,AdminOrdersViewHolder> adapter =
                new FirebaseRecyclerAdapter<AdminOrders, AdminOrdersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminOrdersViewHolder holder,final int position, @NonNull AdminOrders model) {
                        holder.userName.setText("Name: "+ model.getName());
                        holder.userPhoneNumber.setText("Phone: "+ model.getPhone());
                        holder.userTotalPrice.setText("Total Amount: "+ model.getTotalAmount());
                        holder.userDateTime.setText("Order at: "+ model.getDate()+ "  " + model.getTime());
                        holder.userShippingAdress.setText("Adress: "+ model.getAddress()+ "  "+ model.getCity() );

                        holder.showOrdersBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int adapterPosition = holder.getAdapterPosition();
                                if (adapterPosition != RecyclerView.NO_POSITION) {
                                    String uID = getRef(adapterPosition).getKey();
                                    Intent intent = new Intent(AdminNewOrdersActivity.this, AdminUserProductsActivity.class);
                                    intent.putExtra("uid", uID);
                                    startActivity(intent);
                                }
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]
                                        {"YES","No"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminNewOrdersActivity.this);
                                builder.setTitle("Order send to be Delivred ?");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which ==0){
                                            int adapterPosition = holder.getAdapterPosition();
                                            String uID = getRef(adapterPosition).getKey();
                                            RemoveOrder(uID);
                                        }
                                        else {
                                            finish();
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @NonNull
                    @Override
                    public AdminOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.orders_layout,parent,false);
                        return new AdminOrdersViewHolder(view);
                    }
                };
        ordersList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class AdminOrdersViewHolder extends RecyclerView.ViewHolder{

        public TextView userName,userPhoneNumber,userTotalPrice,userDateTime,userShippingAdress;
        public Button showOrdersBtn;
        public AdminOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.order_user_name);
            userPhoneNumber = itemView.findViewById(R.id.order_phone_number);
            userTotalPrice = itemView.findViewById(R.id.order_total_price);
            userDateTime = itemView.findViewById(R.id.order_date_time);
            userShippingAdress = itemView.findViewById(R.id.order_adress_city);
            showOrdersBtn = itemView.findViewById(R.id.show_all_products_btn);
        }
    }

    private void RemoveOrder(String uID){
        orderRef.child(uID).removeValue();
        DatabaseReference adminCartListRef = FirebaseDatabase.getInstance().getReference()
                .child("Cart List")
                .child("Admin View")
                .child(uID)
                .child("Products");

        adminCartListRef.removeValue();
    }
}