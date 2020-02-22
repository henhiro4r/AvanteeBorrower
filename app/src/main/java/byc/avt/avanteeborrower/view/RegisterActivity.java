package byc.avt.avanteeborrower.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import byc.avt.avanteeborrower.R;
import byc.avt.avanteeborrower.model.User;
import byc.avt.avanteeborrower.view.misc.TermFragment;
import byc.avt.avanteeborrower.viewmodel.AuthenticationViewModel;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout editPhoneNumber, editPassword, edtEmail, edtRefId, edtConfirmPassword;
    private String phoneNumber, password, rePassword;
    private Button btnRegister;
    private CheckBox checkAgree;
    private AuthenticationViewModel viewModel;
    private Boolean checkInput = false, readTerm = false;
    private Timer timer;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^" +
            "(?=.*[0-9])" +         //at least has 1 number
            "(?=.*[a-z])" +         //at least has 1 lower case letter
            "(?=.*[A-Z])" +         //at least has 1 upper case letter
            //"(?=.*[a-zA-Z])" + //can be any letter (uppercase/lowercase)
            "(?=.*[@#$%^&+=])" + //at least 1 special character
            "(?=\\\\S+$)." + //no white spaces
            "{8,}" + //at least 8 character
            "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edtEmail = findViewById(R.id.edt_reg_email);
        editPhoneNumber = findViewById(R.id.edt_reg_phone);
        editPassword = findViewById(R.id.edt_reg_password);
        edtConfirmPassword = findViewById(R.id.edt_reg_re_password);
        edtRefId = findViewById(R.id.edt_reg_ref_id);
        btnRegister = findViewById(R.id.btn_register);
        checkAgree = findViewById(R.id.cb_remember_me);
        Toolbar bar = findViewById(R.id.register_toolbar);
        setSupportActionBar(bar);
        viewModel = ViewModelProviders.of(this).get(AuthenticationViewModel.class);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back_24px);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(editPhoneNumber.getEditText()).addTextChangedListener(registerTextWatcher);
        Objects.requireNonNull(editPassword.getEditText()).addTextChangedListener(registerTextWatcher);
        checkAgree.setOnCheckedChangeListener(showTermListener);
    }


    private CompoundButton.OnCheckedChangeListener showTermListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked && TermFragment.read) {
                checkAgree.setChecked(true);
                readTerm = TermFragment.read;
            } else {
                TermFragment termFragment = TermFragment.getInstance();
                termFragment.show(getSupportFragmentManager(), termFragment.getTag());
                checkAgree.setChecked(false);
            }
        }
    };


    private TextWatcher registerTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String phoneNum = editPhoneNumber.getEditText().getText().toString().trim();
            String pass = editPassword.getEditText().getText().toString().trim();
            String rePass = edtConfirmPassword.getEditText().getText().toString().trim();
            checkInput = !phoneNum.isEmpty() && !pass.isEmpty() && !rePass.isEmpty();
//            if (checkInput && readTerm){
//                btnRegister.setEnabled(true);
//            } else {
//                btnRegister.setEnabled(false);
//            }
//            if (timer != null){
//                timer.cancel();
//            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
//            timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    String phoneNum = editPhoneNumber.getEditText().getText().toString().trim();
//                    String pass = editPassword.getEditText().getText().toString().trim();
//                    String rePass = edtConfirmPassword.getEditText().getText().toString().trim();
//                    checkInput = !phoneNum.isEmpty() && !pass.isEmpty() && !rePass.isEmpty();
//                }
//            }, 100);
            if (checkInput && readTerm){
                btnRegister.setEnabled(true);
            } else {
                btnRegister.setEnabled(false);
            }
        }
    };

    private boolean checkPassword(){
        password = editPassword.getEditText().getText().toString().trim();
        rePassword = edtConfirmPassword.getEditText().getText().toString().trim();
        if (rePassword.equals(password)){
            edtConfirmPassword.setError(null);
            return true;
//            if (PASSWORD_PATTERN.matcher(password).matches()){
//                editPassword.setError(null);
//                return true;
//            } else {
//                editPassword.setError("Password terlalu lemah");
//                return false;
//            }
        } else {
            edtConfirmPassword.setError("Kata Sandi Tidak Sama");
            return false;
        }
    }

    private boolean validateEmail(String email){
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError("Email tidak valid");
            return false;
        } else {
            edtEmail.setError(null);
            return true;
        }
    }

    private boolean validatePhoneNumber(String phone){
        if (phone.length() < 12){
            editPhoneNumber.setError("No. Telepon tidak valid");
            return false;
        } else {
            editPhoneNumber.setError(null);
            return true;
        }
    }

    public void confirmInput(View v) {
        phoneNumber = editPhoneNumber.getEditText().getText().toString().trim();
        String email = Objects.requireNonNull(edtEmail.getEditText()).getText().toString().trim();
        String ref_id = Objects.requireNonNull(edtRefId.getEditText()).getText().toString().trim();
        if (!email.isEmpty()){
            checkPassword();
            validatePhoneNumber(phoneNumber);
            validateEmail(email);
        } else {
            if (!checkPassword() | !validatePhoneNumber(phoneNumber)) {
                return;
            }
            // POST to server through endpoint
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setPhoneNumber(phoneNumber);
            viewModel.register(user, ref_id);
            viewModel.getStatus().observe(this, checkStatus);
        }
    }

    private Observer<Boolean> checkStatus = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean aBoolean) {
            if (aBoolean) {
                showMessage("Success");
                //intent to sms verification
            } else {
                showMessage("failed");
            }
        }
    };

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (TermFragment.getInstance().isVisible()){
                TermFragment.getInstance().dismiss();
            } else {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
