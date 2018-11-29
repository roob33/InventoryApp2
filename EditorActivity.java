package com.example.android.inventoryapp2;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    int quantity = 0;
    private static final int EXISTING_INVENTORY_LOADER = 0;
    private Uri mCurrentProductUri;

    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private TextView mProductQuantity;
    private Spinner mProductSupplierNameSpinner;
    private EditText mProductSupplierPhoneNumberEditText;

    private int mSupplierName = InventoryEntry.SUPPLIER_UNKNOWN;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent i = getIntent();
        mCurrentProductUri = i.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        mProductNameEditText = findViewById(R.id.product_name_edit_text);
        mProductPriceEditText = findViewById(R.id.product_price_edit_text);
        mProductQuantity = findViewById(R.id.product_quantity);
        mProductSupplierNameSpinner = findViewById(R.id.product_supplier_name_spinner);
        mProductSupplierPhoneNumberEditText = findViewById(R.id.product_supplier_phone_number_edit_text);
        Button callButton = findViewById(R.id.phone_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mProductSupplierPhoneNumberEditText.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });


        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        mProductSupplierNameSpinner.setOnTouchListener(mTouchListener);
        mProductSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    public void increment(View view) {
        if (quantity >= 0) {
        }
        quantity = quantity + 1;
        productQuantity(quantity);
    }

    public void decrement(View view) {
        if (quantity == 0) {
            return;
        }
        quantity = quantity - 1;
        productQuantity(quantity);
    }


    private void productQuantity(int number) {
        TextView quantityText = findViewById(R.id.product_quantity);
        quantityText.setText("" + number);
    }

    private void setupSpinner() {

        ArrayAdapter supplierNameSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        supplierNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mProductSupplierNameSpinner.setAdapter(supplierNameSpinnerAdapter);

        mProductSupplierNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_ikea))) {
                        mSupplierName = InventoryEntry.SUPPLIER_IKEA;
                    } else if (selection.equals(getString(R.string.supplier_amazon))) {
                        mSupplierName = InventoryEntry.SUPPLIER_AMAZON;
                    } else if (selection.equals(getString(R.string.supplier_shein))) {
                        mSupplierName = InventoryEntry.SUPPLIER_SHEIN;
                    } else if (selection.equals(getString(R.string.supplier_jarirr))) {
                        mSupplierName = InventoryEntry.SUPPLIER_JARIRR;
                    } else {
                        mSupplierName = InventoryEntry.SUPPLIER_UNKNOWN;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSupplierName = InventoryEntry.SUPPLIER_UNKNOWN;
            }
        });
    }

    private void saveProduct() {

        String productNameString = mProductNameEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString().trim();
        String productQuantityString = mProductQuantity.getText().toString().trim();
        String productSupplierPhoneNumberString = mProductSupplierPhoneNumberEditText.getText().toString().trim();
        if (mCurrentProductUri == null) {
            if (TextUtils.isEmpty(productNameString)) {
                Toast.makeText(this, getString(R.string.product_name_requires), Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(productPriceString)) {
                Toast.makeText(this, getString(R.string.price_requires), Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(productQuantityString)) {
                Toast.makeText(this, getString(R.string.quantity_requires), Toast.LENGTH_LONG).show();
                return;
            }
            if (mSupplierName == InventoryEntry.SUPPLIER_UNKNOWN) {
                Toast.makeText(this, getString(R.string.supplier_name_requires), Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(productSupplierPhoneNumberString)) {
                Toast.makeText(this, getString(R.string.supplier_phone_requires), Toast.LENGTH_LONG).show();
                return;
            }


            ContentValues values = new ContentValues();

            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, mSupplierName);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, productSupplierPhoneNumberString);

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_failed), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_successful), Toast.LENGTH_LONG).show();

                finish();
            }
        } else {
            if (TextUtils.isEmpty(productNameString)) {
                Toast.makeText(this, getString(R.string.product_name_requires), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(productPriceString)) {
                Toast.makeText(this, getString(R.string.price_requires), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(productQuantityString)) {
                Toast.makeText(this, getString(R.string.quantity_requires), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mSupplierName == InventoryEntry.SUPPLIER_UNKNOWN) {
                Toast.makeText(this, getString(R.string.supplier_name_requires), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(productSupplierPhoneNumberString)) {
                Toast.makeText(this, getString(R.string.supplier_phone_requires), Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues values = new ContentValues();

            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, mSupplierName);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, productSupplierPhoneNumberString);

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;

            case R.id.delete_button:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);

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
        DialogInterface.OnClickListener disCardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(disCardButtonClickListener);

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


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);

            String currentName = cursor.getString(nameColumnIndex);
            int currentPrice = cursor.getInt(priceColumnIndex);
            int currentQuantity = cursor.getInt(quantityColumnIndex);
            int currentSupplierName = cursor.getInt(supplierNameColumnIndex);
            int currentSupplierPhone = cursor.getInt(supplierPhoneColumnIndex);

            mProductNameEditText.setText(currentName);
            mProductPriceEditText.setText(Integer.toString(currentPrice));
            mProductQuantity.setText(Integer.toString(currentQuantity));
            mProductSupplierPhoneNumberEditText.setText(Integer.toString(currentSupplierPhone));

            switch (currentSupplierName) {
                case InventoryEntry.SUPPLIER_AMAZON:
                    mProductSupplierNameSpinner.setSelection(1);
                    break;
                case InventoryEntry.SUPPLIER_IKEA:
                    mProductSupplierNameSpinner.setSelection(2);
                    break;
                case InventoryEntry.SUPPLIER_JARIRR:
                    mProductSupplierNameSpinner.setSelection(3);
                    break;
                case InventoryEntry.SUPPLIER_SHEIN:
                    mProductSupplierNameSpinner.setSelection(4);
                    break;
                default:
                    mProductSupplierNameSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantity.setText("");
        mProductSupplierPhoneNumberEditText.setText("");
        mProductSupplierNameSpinner.setSelection(0);

    }


    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
