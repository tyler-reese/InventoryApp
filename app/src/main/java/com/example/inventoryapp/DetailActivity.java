package com.example.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.data.ProductContract.ProductEntry;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    private EditText mQuantityModifierEditText;

    private int mQuantity = 0;
    private static final int MAX_QUANTITY_MODIFIER = 1000;
    private static final int MAX_QUANTITY = 1000000;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.detail_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.detail_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityTextView = findViewById(R.id.product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumberEditText = findViewById(R.id.edit_supplier_phone_number);
        mQuantityModifierEditText = findViewById(R.id.quantity_modifier);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
    }

    private boolean saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Product name must be set",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        double price = 0.0;
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Price must be set",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            price = Float.parseFloat(priceString);
            if (price < 0.0) {
                Toast.makeText(this, "Price must not be negative",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        int quantity = 0;
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Quantity must be set",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            quantity = Integer.parseInt(quantityString);
            if (quantity < 0) {
                Toast.makeText(this, "Quantity must be >= 0",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, "Supplier name must be set",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(supplierPhoneNumberString)) {
            Toast.makeText(this, "Supplier phone number must be set",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.detail_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.detail_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveProduct())
                    finish();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this,
                mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);

            String name = cursor.getString(nameColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(String.format(Locale.ENGLISH, "%.2f", price));
            mQuantityTextView.setText(String.format(Locale.ENGLISH, "%d", mQuantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteProduct(View view) {
        showDeleteConfirmationDialog();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendDeleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void sendDeleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.detail_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    public void callSupplier(View view) {
        String phoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private int getQuantityModifier() {
        int modifier = 1;
        String quantityModifierString = mQuantityModifierEditText.getText().toString();
        if (!TextUtils.isEmpty(quantityModifierString)) {
            try {
                modifier = Integer.parseInt(quantityModifierString);
            } catch (NumberFormatException e) {
                modifier = MAX_QUANTITY_MODIFIER;
            } finally {
                if (modifier > MAX_QUANTITY_MODIFIER)
                    modifier = MAX_QUANTITY_MODIFIER;
                mQuantityModifierEditText.setText(String.format(Locale.ENGLISH, "%d", modifier));
            }
        }
        return modifier;
    }

    public void increaseQuantity(View view) {
        Integer.valueOf(mQuantityTextView.getText().toString());
        int quantity = getQuantityModifier();
        mQuantity += quantity;
        if (mQuantity > MAX_QUANTITY)
            mQuantity = MAX_QUANTITY;
        mQuantityTextView.setText(String.format(Locale.ENGLISH, "%d", mQuantity));
    }

    public void decreaseQuantity(View view) {
        int quantity = getQuantityModifier();
        mQuantity -= quantity;
        if (mQuantity < 0)
            mQuantity = 0;
        mQuantityTextView.setText(String.format(Locale.ENGLISH, "%d", mQuantity));
    }
}