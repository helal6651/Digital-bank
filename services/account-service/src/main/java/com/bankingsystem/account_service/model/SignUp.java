package com.bankingsystem.account_service.model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUp {

    private String userName;
    private String password;

    public SignUp() {

    }


    public SignUp(String userName, String password) {

        this.userName = userName;
        this.password = password;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }



}