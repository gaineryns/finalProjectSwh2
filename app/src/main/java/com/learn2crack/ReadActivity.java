package com.learn2crack;//////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2016 (Basse, Volga)                                            //
//                                                                              //
// Ce programme (ainsi que la bibliothèque objets livrée avec le livre)         //
// est libre, vous pouvez le redistribuer et/ou                                 //
// le modifier selon les termes de la                                           //
// Licence Publique Générale GNU publiée par la                                 //
// Free Software Foundation (version 2 ou bien toute autre version              //
// ultérieure choisie par vous).                                                //
// Ce programme est distribué car potentiellement utile,                        //
// mais SANS AUCUNE GARANTIE, ni explicite ni implicite,                        //
// y compris les garanties de commercialisation ou                              //
// d'adaptation dans un but spécifique. Reportez-vous à la                      //
// Licence Publique Générale GNU pour plus de détails.                          //
// Vous devez avoir reçu une copie de la Licence                                //
// Publique Générale GNU en même temps que                                      //
// ce programme ; si ce n'est pas le cas, écrivez à la                          //
// Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA       //
// 02111-1307, États-Unis.                                                      //
//                                                                              //
// Différents sites donnent des copies officielles ou non de cette licence :    //
// http://www.linux-france.org/article/these/gpl.html                           //
// http://www.gnu.org/copyleft/gpl.html                                         //
//////////////////////////////////////////////////////////////////////////////////

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ReadActivity extends AppCompatActivity {

    protected PendingIntent mPendingIntent;
    protected static IntentFilter[] mIntentFiltersArray;
    protected static String[][] mTechListsArray;
    protected NfcAdapter mNfcAdapter;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_APP = "application/octet-stream";
    public static final String TAG = "NfcDemo";

    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String type;
    private String[] technology;
    private int size;
    private byte[] ID_tag;

    private LinearLayout mContentLayout;
    private RelativeLayout mScanLayout;
    private TextView mContentTV;
    private TextView mTypeTV;
    private TextView mTechTV;
    private TextView mSizeTV;
    private TextView mIdTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mScanLayout = (RelativeLayout) findViewById(R.id.scan_layout);
        mContentLayout = (LinearLayout) findViewById(R.id.content_layout);
        mContentTV = (TextView) findViewById(R.id.content_textView);
        mTypeTV = (TextView) findViewById(R.id.type_textView);
        mTechTV = (TextView) findViewById(R.id.tech_textView);
        mSizeTV = (TextView) findViewById(R.id.size_textView);
        mIdTV = (TextView) findViewById(R.id.id_textView);



        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }

        // Android system will populate it with the details of the tag when it is scanned
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //Launched by tag scan ?
        Tag tag = (Tag) getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null && (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)
                || getIntent().getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)
                || getIntent().getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))) {
            Log.i(TAG, "onCreate, tag found, calling onNewTag");
            getNewTag(tag, getIntent());
        }

    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            technology = tag.getTechList();
            Ndef ndef = Ndef.get(tag);
            size = ndef.getMaxSize();
            type = intent.getType();
            ID_tag = tag.getId();

            if (MIME_TEXT_PLAIN.equals(type) || MIME_APP.equals(type)) {
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            technology = tag.getTechList();
            Ndef ndef = Ndef.get(tag);
            size = ndef.getMaxSize();
            type = intent.getType();
            ID_tag = tag.getId();
            String searchedTech = Ndef.class.getName();

            for (String tech : technology) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
        else if (intent.getType() != null && intent.getType().equals("application/" + getPackageName())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefRecord relayRecord = ((NdefMessage) rawMsgs[0]).getRecords()[0];
            String nfcData = new String(relayRecord.getPayload());
            Toast.makeText(this, nfcData, Toast.LENGTH_SHORT).show();
        }
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                } else if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_URI)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();
            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? new String("UTF-8") : "UTF-16";
            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;
            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {

            StringBuilder sb = new StringBuilder();
            if (result != null) {
                sb.append(technology[0].toString().split("\\.")[3]);
                for (int i = 1; i < technology.length; i++) {
                    sb.append(", " + technology[i].toString().split("\\.")[3]);
                }
                mContentTV.setText("   " + result);
                mTypeTV.setText("   " + type);
                mTechTV.setText("   " + sb.toString());
                mSizeTV.setText("   " + size + " Bytes");
                mIdTV.setText("   " + bytesToHex(ID_tag));
            } else {
                mContentTV.append("TAG vide");
            }

            mScanLayout.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[((bytes.length) * 3) - 1];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if (j != bytes.length - 1)
                hexChars[j * 3 + 2] = ':';
        }
        return new String(hexChars);
    }

    public void onNewTag(Tag tag) {
        handleIntent(getIntent());
    }

    static {

        // add intent filter
        IntentFilter mndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter mtech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter mtag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            //Handles all MIME based dispatches !!! specify only the ones that you need.
            mndef.addDataType("*/*");

        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mIntentFiltersArray = new IntentFilter[]{mndef, mtech/*, mtag*/};
        //array of TAG TECHNOLOGIES that your application wants to handle
        mTechListsArray = new String[][]{};
    }

    private void getNewTag(Tag tag, Intent intent) {
        if (tag == null) return;
        //Indicate to childs that a new tag has been detected
        onNewTag(tag);
    }

    protected void enableWriteMode() {
        IntentFilter mndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter mtech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter mtag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mIntentFiltersArray = new IntentFilter[]{mndef,mtech,mtag};

        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFiltersArray, null);
    }

    protected void disableWriteMode() {
        mNfcAdapter.disableForegroundDispatch(this);
    }

    protected void displayMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
        toast.show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // get the tag object for the discovered tag
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        getNewTag(tag, intent);
    }

    //This function is called in child activities when a new tag is scanned.

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
