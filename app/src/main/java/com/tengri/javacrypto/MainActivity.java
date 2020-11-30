package com.tengri.javacrypto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.String;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<Message> adapter;
    LinearLayout activity_main;
    ImageButton send;
    EditText input;
    ListView listMessages;

    public static String TAG = "MainActivity";

    public static String alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (LinearLayout) findViewById(R.id.activity_main);
        listMessages = (ListView) findViewById(R.id.listView);
        send = (ImageButton) findViewById(R.id.send_btn);
        input = (EditText) findViewById(R.id.input);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//                ref.removeValue();
                String message = input.getText().toString();
                if (message.equals("")){
                    Toast.makeText(getApplicationContext(), "Input empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    String cipher = encryptAlgorithm(message);

                    String des_cipher = DES_encrypt(cipher).replaceAll("\\n", " ");

                    FirebaseDatabase.getInstance().getReference().push().setValue(new Message(des_cipher,
                            FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                    input.setText("");
                }
            }
        });
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), SIGN_IN_REQUEST_CODE);
        }
        else {
            displayChat();
        }
    }



    //Vigenere----------------------------------------------------------------
    private String encryptAlgorithm(String text) {
        String keyphrase = "qwerty";
        keyphrase = keyphrase.toUpperCase();
        StringBuilder sb = new StringBuilder(100);

        for (int i = 0, j = 0; i < text.length(); i++) {

            char upper = text.toUpperCase().charAt(i);
            char orig = text.charAt(i);

            if (upper < 128){
                if (Character.isAlphabetic(orig)) {
                    if (Character.isUpperCase(orig)) {
                        sb.append((char)((upper + keyphrase.charAt(j) - 130) % 26 + 65));
                        ++j;
                        j %= keyphrase.length();
                    } else {
                        sb.append(Character.toLowerCase((char)((upper + keyphrase.charAt(j) - 130) % 26 + 65)));
                        ++j;
                        j %= keyphrase.length();
                    }
                } else {
                    sb.append(orig);
                }
            }

            else{
                keyphrase = "йцукен";
                keyphrase = keyphrase.toUpperCase();


                if (Character.isAlphabetic(orig)) {
                    if (Character.isUpperCase(orig)) {
                        int upper_index = alphabet.indexOf(upper);
                        int key_index = alphabet.indexOf(keyphrase.charAt(j));
                        int value = (upper_index + key_index ) % 33;
                        sb.append((char)(alphabet.charAt(value)));
                        Log.d(TAG, "Text: "+ upper_index + ", Key:" + key_index + ", Value: " +value );
                        ++j;
                        j %= keyphrase.length();
                    } else {
                        int upper_index = alphabet.indexOf(upper);
                        int key_index = alphabet.indexOf(keyphrase.charAt(j));
                        int value = (upper_index + key_index ) % 33;
                        sb.append(Character.toLowerCase((char)(alphabet.charAt(value))));
                        Log.d(TAG, "Text: "+ upper_index + ", Key:" + key_index + ", Value: " +value );
                        ++j;
                        j %= keyphrase.length();
                    }
                } else {
                    sb.append(orig);
                }
            }



        }
        return sb.toString();
    }

    private String decryptAlgorithm(String text) {
        String keyphrase = "qwerty";
        keyphrase = keyphrase.toUpperCase();
        StringBuilder sb = new StringBuilder(100);

        for (int i = 0, j = 0; i < text.length(); i++) {

            char upper = text.toUpperCase().charAt(i);
            char orig = text.charAt(i);
            if (upper<192){
                if (Character.isAlphabetic(orig)) {
                    if (Character.isUpperCase(orig)) {
                        sb.append((char)((upper - keyphrase.charAt(j) + 26) % 26 + 65));
                        ++j;
                        j %= keyphrase.length();
                    } else {
                        sb.append(Character.toLowerCase((char)((upper - keyphrase.charAt(j) + 26) % 26 + 65)));
                        ++j;
                        j %= keyphrase.length();
                    }
                } else {
                    sb.append(orig);
                }
            }
            else {
                keyphrase = "йцукен";
                keyphrase = keyphrase.toUpperCase();
                    if (Character.isAlphabetic(orig)) {
                        if (Character.isUpperCase(orig)) {
                            int upper_index = alphabet.indexOf(upper);
                            int key_index = alphabet.indexOf(keyphrase.charAt(j));
                            int value = (upper_index - key_index + 33) % 33;
                            sb.append((char)(alphabet.charAt(value)));
                            ++j;
                            j %= keyphrase.length();
                        } else {
                            int upper_index = alphabet.indexOf(upper);
                            int key_index = alphabet.indexOf(keyphrase.charAt(j));
                            int value = (upper_index - key_index + 33) % 33;
                            sb.append(Character.toLowerCase((char)(alphabet.charAt(value))));

                            ++j;
                            j %= keyphrase.length();
                        }
                    } else {
                        sb.append(orig);
                    }

            }
            }



        return sb.toString();
    }

//----------------------------------------------------------------
    private DatabaseReference myRef;

    //DES
    public static String des_key = "qwertyui";

    public String DES_encrypt(String value) {

        String crypted = "";

        try {

            byte[] cleartext = value.getBytes("UTF-8");

            SecretKeySpec key = new SecretKeySpec(des_key.getBytes(), "DES");

            Cipher cipher = Cipher.getInstance("DES/ECB/ZeroBytePadding");

            // Initialize the cipher for decryption
            cipher.init(Cipher.ENCRYPT_MODE, key);

            crypted = android.util.Base64.encodeToString(cipher.doFinal(cleartext), android.util.Base64.DEFAULT);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            return "Encrypt Error";
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return "Encrypt Error";
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();

            return "Encrypt Error";
        }
        catch (BadPaddingException e) {
            e.printStackTrace();

            return "Encrypt Error";
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();

            return "Encrypt Error";
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        }
        catch (Exception e) {
            e.printStackTrace();

            return "Encrypt Error";
        }

        return crypted;
    }

    public String DES_decrypt(String value) {

        String coded;
        if(value.startsWith("code==")){
            coded = value.substring(6,value.length()).trim();
        }else{
            coded = value.trim();
        }

        String result = null;

        try {
            // Decoding base64
            byte[] bytesDecoded = android.util.Base64.decode(coded.getBytes("UTF-8"), android.util.Base64.DEFAULT);

            SecretKeySpec key = new SecretKeySpec(des_key.getBytes(), "DES");

            Cipher cipher = Cipher.getInstance("DES/ECB/ZeroBytePadding");

            // Initialize the cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, key);

            // Decrypt the text
            byte[] textDecrypted = cipher.doFinal(bytesDecoded);

            result = new String(textDecrypted);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Decrypt Error";
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return "Decrypt Error";
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return "Decrypt Error";
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
            return "Decrypt Error";
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
            return "Decrypt Error";
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Decrypt Error";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Decrypt Error";
        }

        return result;
    }

    @SuppressLint("ResourceAsColor")
    private void displayChat(){
        myRef = FirebaseDatabase.getInstance().getReference();
        FirebaseListOptions<Message> options = new FirebaseListOptions.Builder<Message>()
                .setQuery(myRef, Message.class)
                .setLayout(R.layout.item_message)
                .build();

        adapter = new FirebaseListAdapter<Message>(options) {
            @SuppressLint("ResourceAsColor")
            @Override
            protected void populateView(View v, Message model, int position) {

                RelativeLayout relativeLayout= (RelativeLayout) v.findViewById(R.id.relative);
                TextView textMessage, author, timeMessage, tvCipher, textCipher1;
                tvCipher = (TextView) v.findViewById(R.id.tvCipher);
                textMessage = (TextView)v.findViewById(R.id.tvMessage);
                author = (TextView)v.findViewById(R.id.tvUser);
                timeMessage = (TextView)v.findViewById(R.id.tvTime);
                textCipher1 = (TextView) v.findViewById(R.id.tvCipher1);
                String des_decipher = DES_decrypt(model.getTextMessage());
                textCipher1.setText(des_decipher);


                String decipher = decryptAlgorithm(des_decipher);

                tvCipher.setText(decipher);
                textMessage.setText(model.getTextMessage());
                author.setText(model.getAutor());
                timeMessage.setText(DateFormat.format("dd.MM.yyyy HH:mm:ss", model.getTimeMessage()));

                if (position % 2 ==0){
                    relativeLayout.setBackgroundColor(R.color.colorAccent);
                }
                else if (position % 2 ==1){
                    relativeLayout.setBackgroundColor(R.color.gray);
                }
            }
        };
        adapter.startListening();
        listMessages.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Snackbar.make(activity_main, "Вход выполнен", Snackbar.LENGTH_SHORT).show();
                displayChat();
            } else {
                Snackbar.make(activity_main, "Вход не выполнен", Snackbar.LENGTH_SHORT).show();
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
        if (item.getItemId() == R.id.menu_signout)
        {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Snackbar.make(activity_main, "Выход выполнен", Snackbar.LENGTH_SHORT).show();
                            finish();

                        }
                    });
        }
        return true;
    }

    @Override
    protected void onPause () {

        // скрываем клавиатуру, чтобы избежать getTextBeforeCursor при неактивном InputConnection
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService (Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow (input.getWindowToken (), 0);

        super.onPause ();
    }
}
