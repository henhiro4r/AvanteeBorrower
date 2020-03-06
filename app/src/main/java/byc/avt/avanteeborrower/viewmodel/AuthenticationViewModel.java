package byc.avt.avanteeborrower.viewmodel;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import byc.avt.avanteeborrower.BuildConfig;
import byc.avt.avanteeborrower.model.User;

public class AuthenticationViewModel extends ViewModel {
    private MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();


    public void register(User user, String ref_id){
        isSuccess.setValue(true);
    }

    public LiveData<Boolean> getStatus(){
        return isSuccess;
    }
}
