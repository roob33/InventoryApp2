package com.example.android.inventoryapp2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp2.data.InventoryContract;
import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup FAB EditorActivity
        FloatingActionButton fab = findViewById(R.id.plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(i);
            }
        });

        ListView inventoryList = findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        inventoryList.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryList.setAdapter(mCursorAdapter);

        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                i.setData(currentInventoryUri);
                startActivity(i);
            }
        });
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }

    public void productSale(int productID, int productQuantity){
        productQuantity = productQuantity -1;
        if (productQuantity >= 0){
            ContentValues  values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
            Uri updateUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productID);
            int rowsAffected = getContentResolver().update(updateUri, values, null, null);
            Toast.makeText(this, "Quantity was change", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Product was finish",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Toast.makeText(this, rowsDeleted + " " + getString(R.string.deleted_all_products_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            //click on the "Delete all entries"
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deleted_all_products_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteAllEntries();
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
