package com.sujay.apps.sarvamart;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<FirebaseUser> currUser = new MutableLiveData<>(null);
    private boolean hasShownLogin;

    public MutableLiveData<FirebaseUser> getCurrUser() {
        return currUser;
    }

    public void loadCurrUser() {
//        currUser.setValue(user);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currUser.setValue(currentUser);
    }

    public boolean getLogin() {
        return hasShownLogin;
    }

    public void setLogin(boolean login) {
        hasShownLogin = login;
    }
}
