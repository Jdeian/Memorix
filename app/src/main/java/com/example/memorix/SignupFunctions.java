package com.example.memorix;

import android.widget.EditText;

import com.google.android.material.checkbox.MaterialCheckBox;

public class SignupFunctions {

    private final EditText txtfirstname;
    private final EditText txtlastname;
    private final EditText txtbirthdate;
    private final EditText txtnickname;
    private final EditText txtemail;
    private final EditText txtpassword;
    private final EditText txtconfirm_password;
    private final MaterialCheckBox termsAndConditions;

    public SignupFunctions(EditText txtfirstname, EditText txtlastname, EditText txtbirthdate,
                           EditText txtnickname, EditText txtemail, EditText txtpassword, EditText txtconfirm_password, MaterialCheckBox termsAndConditions) {
        this.txtfirstname = txtfirstname;
        this.txtlastname = txtlastname;
        this.txtbirthdate = txtbirthdate;
        this.txtnickname = txtnickname;
        this.txtemail = txtemail;
        this.txtpassword = txtpassword;
        this.txtconfirm_password = txtconfirm_password;
        this.termsAndConditions = termsAndConditions;
    }

    public boolean isEmptyFirstname() {
        return txtfirstname.getText().toString().trim().isEmpty();
    }

    public boolean isEmptyLastname() {
        return txtlastname.getText().toString().trim().isEmpty();
    }

    public boolean isEmptyBirthdate() {
        return txtbirthdate.getText().toString().trim().isEmpty();
    }

    public boolean isValidBirthdate() {
        String birthdate = txtbirthdate.getText().toString().trim();
        String birthdatePattern = "^(\\d{4})/(\\d{2})/(\\d{2})$";
        return !birthdate.matches(birthdatePattern);
    }

    public boolean isEmptyNickname() {
        return txtnickname.getText().toString().trim().isEmpty();
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

    public boolean isValidPasswordLength() {
        return txtpassword.getText().toString().trim().length() < 8;
    }

    public boolean isEmptyConfirmpassword() {
        return txtconfirm_password.getText().toString().trim().isEmpty();
    }

    public boolean isConfirmpassMatchPass() {
        return !txtconfirm_password.getText().toString().trim().equals(txtpassword.getText().toString().trim());
    }

    public boolean isNotCheckedTermsAndConditions() {
        return !termsAndConditions.isChecked();
    }
}
