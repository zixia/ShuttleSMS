package com.akamobi.shuttlesms;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.akamobi.shuttlesms.db.CreditDb;

public class CreditDetail extends Activity {

    private ImageView 		mRuleType;
    private EditText 		mRuleKeyword;
    private EditText		mRuleNote;
    
    private Long			mRowId;
    private Long			mTypeId;
    
    private CreditDb mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = new CreditDb(this);
        mDb.open();

        setContentView(R.layout.credit_edit);
        setTitle(R.string.credit_list);
        
        mRuleKeyword 	= (EditText) findViewById(R.id.rule_keyword);
        mRuleNote 		= (EditText) findViewById(R.id.rule_note);

        Button confirmButton 	= (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(CreditDb.KEY_ROWID);


		if (mRowId == null) {	// new or edit exist rule
			Bundle extras = getIntent().getExtras();
			
			if (null!=extras) {
				Long i;
				i = extras.getLong(CreditDb.KEY_ROWID);
				
				if ( i>0 ){	// Edit exist rule
					mRowId = i;
				}else{		// New rule
					mTypeId 	= extras.getLong(CreditDb.KEY_TYPE);
				}
				
			}
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor credit = mDb.fetchCredit(mRowId);
            startManagingCursor(credit);
            //mRuleType.
            mRuleKeyword.setText(credit.getString(
                    credit.getColumnIndexOrThrow(CreditDb.KEY_WORD)));
            mRuleNote.setText(credit.getString(
                    credit.getColumnIndexOrThrow(CreditDb.KEY_NOTE)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(CreditDb.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        int	type		= CreditDb.TYPE_WHITENUM;
    	String keyword 	= mRuleKeyword.getText().toString();
        String note 	= mRuleNote.getText().toString();

        if (mRowId == null) {
            long id = mDb.createCredit(type, keyword, note);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDb.updateCredit(mRowId, type, keyword, note);
        }
    }

} 