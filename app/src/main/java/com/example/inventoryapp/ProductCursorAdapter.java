package com.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.inventoryapp.data.ProductContract.ProductEntry;

import java.util.Locale;

public class ProductCursorAdapter extends CursorAdapter {

    ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);

        final int columnIdIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        final String productID = cursor.getString(columnIdIndex);
        String productName = cursor.getString(nameColumnIndex);
        Float price = cursor.getFloat(priceColumnIndex);
        Integer quantity = cursor.getInt(quantityColumnIndex);

        nameTextView.setText(productName);
        priceTextView.setText(String.format(Locale.ENGLISH, "$%.2f", price));
        quantityTextView.setText(String.format(Locale.ENGLISH, "%d", quantity));

        Button saleButton = view.findViewById(R.id.sell);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) context;
                activity.productSale(v, Integer.valueOf(productID));
            }
        });
    }
}
