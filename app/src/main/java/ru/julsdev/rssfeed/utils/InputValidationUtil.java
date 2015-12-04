package ru.julsdev.rssfeed.utils;

import android.support.design.widget.TextInputLayout;

public class InputValidationUtil {

    public static boolean validateFeedFields(String feedName, TextInputLayout feedNameWrapper, String feedUrl, TextInputLayout feelUrlWrapper) {
        boolean isValid = true;

        if (feedName.trim().length() == 0) {
            feedNameWrapper.setError("Enter feed name please");
            isValid = false;
        }
        if (feedUrl.trim().length() == 0) {
            feelUrlWrapper.setError("Enter feed address please");
            isValid = false;
        }

        return isValid;
    }
}
