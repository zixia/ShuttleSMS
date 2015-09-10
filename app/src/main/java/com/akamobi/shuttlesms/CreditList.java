package com.akamobi.shuttlesms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.akamobi.shuttlesms.db.CreditDb;

public class CreditList extends ListActivity {
    private static final int ACTIVITY_CREATE	= 1;
    private static final int ACTIVITY_EDIT		= 2;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1; 
    
	private CreditDb mDb;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Shuttle shuttle 		= new Shuttle(this.getApplicationContext());
        Shuttle.Action action	= shuttle.Process("+8613601369912", "TEST");
        String replyMessage 	= shuttle.getReplyMessage();
*/
        
        setContentView(R.layout.credit_list);
        mDb = new CreditDb(this);
        mDb.open();
        fillData();
        registerForContextMenu(getListView());

    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor cursor = mDb.fetchAllCredits();
        startManagingCursor(cursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{
        		CreditDb.KEY_TYPE
        		, CreditDb.KEY_WORD
        		//, CreditDb.KEY_ENABLED
        };
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{
        		 R.id.rule_type
        		, R.id.rule_keyword
        		//, R.id.rule_enabled
        };
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter credits = 
        	    new SimpleCursorAdapter(this, R.layout.credit_row, cursor, from, to);
        setListAdapter(credits);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID,0, R.string.add);
        return true;
    }

    protected Dialog onCreateDialog(int id) {
        final String[] items = new String[] {
            	"White Number"
            	, "Black Number"
            	, "White Keyword"
            	, "Black Keyword"
            };
        
        final int dbMap[] = {
        		CreditDb.TYPE_WHITENUM
        		, CreditDb.TYPE_BLACKNUM
        		, CreditDb.TYPE_WHITEKEY
        		, CreditDb.TYPE_BLACKKEY
        };

        return new AlertDialog.Builder(CreditList.this)
        .setTitle(R.string.select_credit_type)
        .setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                /* User clicked so do some stuff */                
                new AlertDialog.Builder(CreditList.this)
                        .setMessage("You selected: " + which + " , " + items[which])
                        .show();
                createCredit(dbMap[which]);

            }
        })
        .create();

    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case INSERT_ID:
        	showDialog(0);
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.credit_list);

        // TODO: fill in rest of method
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch( item.getItemId() ) {
		case DELETE_ID:
	        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        mDb.deleteCredit(info.id);
	        fillData();
	        return true;
		}
    	return super.onContextItemSelected(item);
	}

    private void createCredit(int type) {
    	Intent i = new Intent(this, CreditDetail.class);
    	i.putExtra(CreditDb.KEY_TYPE, type);
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Intent i = new Intent(this, CreditDetail.class);
        i.putExtra(CreditDb.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        fillData();
    }

}
