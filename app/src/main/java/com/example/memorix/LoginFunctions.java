package com.example.memorix;

import android.widget.EditText;

public class LoginFunctions {
    private final EditText txtemail;
    private final EditText txtpassword;

    public LoginFunctions(EditText txtemail, EditText txtpassword) {
        this.txtemail = txtemail;
        this.txtpassword = txtpassword;
    }

   public boolean isEmptyEmail() {
        return txtemail.getText().toString().trim().isEmpty();
   }

   public boolean isValidEmail() {
        String email = txtemail.getText().toString().trim();
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$";
        return !email.matches(emailPattern);
    }

    public boolean isEmptyPassword() {
        return txtpassword.getText().toString().trim().isEmpty();
    }
}
