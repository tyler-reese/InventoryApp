package com.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.data.ProductContract.ProductEntry;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    public String generateString() {
        byte[] array = new byte[6];
        new Random().nextBytes(array);
        return android.util.Base64.encodeToString(array, android.util.Base64.DEFAULT).trim();
    }

    private void insertDummyProduct() {
        Random rand = new Random();
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Product " + generateString());
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 9.99);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, rand.nextInt(1000 + 1));
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "Supplier " + generateString());
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, "972-555-1212");
        getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    private void deleteAllProducts() {
        getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyProduct();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY
        };

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public void productSale(View v, int productID) {
        RelativeLayout parent = (RelativeLayout) v.getParent();
        TextView quantityTextView = parent.findViewById(R.id.quantity);
        int quantity = Integer.parseInt(quantityTextView.getText().toString());
        quantity -= 1;
        if (quantity < 0) {
            Toast.makeText(this, getString(R.string.no_quantity),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productID);
        getContentResolver().update(currentProductUri, values, null, null);

        quantityTextView.setText(String.format(Locale.ENGLISH, "%d", quantity));
    }
}
