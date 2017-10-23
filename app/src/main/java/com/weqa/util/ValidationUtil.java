package com.weqa.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Manish Ballav on 10/11/2017.
 */

public class ValidationUtil {

    // validating email id
    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidMobile(String mobile) {
        String regex = "[^\\d]";
        String digits = mobile.replaceAll(regex, "");
//        if(digits.length() != 10) {
        if(digits.length() < 10 || digits.length() > 13) {
            return false;
        }
        else
        {
            return true;
        }
    }

}
