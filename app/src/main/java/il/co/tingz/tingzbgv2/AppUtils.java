package il.co.tingz.tingzbgv2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AppUtils {

    private static final String mAPPLICATION = "testworker";
    private static String sUUID;
    private static Context sContext;

    public static void init(Context context) {
        sUUID = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
        sContext = context;
    }

    @SuppressWarnings("static-access")
    public static String getPreferences(Context context, String name){
        return context.getSharedPreferences(mAPPLICATION, context.MODE_PRIVATE)
                .getString(name, "");
    }

    @SuppressWarnings("static-access")
    public static void setPreferences(Context context, String name, String value){
        SharedPreferences.Editor edit = context.getSharedPreferences(mAPPLICATION, context.MODE_PRIVATE).edit();
        if(edit == null)
            return;
        //edit.clear();
        edit.putString(name, value);
        edit.commit();
    }

    public static String getUUID() {
        return sUUID;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view == null) {
            view = activity.getWindow().getDecorView().getRootView();
        }
        inputMethodManager.hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    public static int dpToPx(int dp)
    {
//      float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ht, sContext.getResources().getDisplayMetrics());
        return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return Math.round(px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getMeasureUnitFromDp(Context contex, int sizeInDP) {

        Resources r = contex.getResources();

        int size = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, r
                        .getDisplayMetrics());

        return size;
    }

    public static boolean validateId(String mId){
        try {
            if(mId==null || mId.trim().matches("[0]+"))
                return false;
            String fullID = String.format("%09d", Integer.parseInt(mId));
            Integer chkSum = Integer.parseInt(TextUtils.substring(fullID, 8, 9));
            Integer idNum = checkDigit(Integer.parseInt(TextUtils.substring(fullID, 0, 8)));
            if (idNum != chkSum)
                return false;
            else
                return true;
        }
        catch(Exception ex) {
            return true;
        }
    }

    public static int checkDigit(int num){
        int digit, sum=0;
        for (int i = 1; i <= 8; i++)
        {
            digit = num % 10;
            if (i % 2 != 0) digit *= 2;
            sum += digit % 10 + digit / 10;
            num = num / 10;
        }
        int checkdigit  = (1 + sum / 10) * 10 - sum;
        checkdigit = checkdigit % 10;
        return checkdigit;
    }
    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(2*(int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static String dateFormat(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String dateFormat(long time, String format) {
        Date date = new Date();
        date.setTime(time);
        return dateFormat(date, format);
    }

    public static Date dateFromString(String data, String pattern){
        Date date = null;
        SimpleDateFormat formater = new SimpleDateFormat(pattern);
        try{
            date = formater.parse(data);
        } catch(ParseException e){
            Log.e("dateFromString", "Can't parse date '" + data + "' with pattern '" + pattern + "'", e);
        }
        return date;
    }




//    public static void showSimpleAlertDialog(Context context, String title, String message) {
//        showSimpleAlertDialog(context, title, message, null);
//    }

//    public static void showSimpleAlertDialog(Context context, String title, String message, final Action<Void> action) {
//        AlertDialog ad = new AlertDialog.Builder(context)
//                .setTitle(title)
//                .setMessage(message)
//                .setCancelable(true)
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        if(action!=null)
//                            action.onAction(null);
//                    }
//                })
//                .show();
//     //   ad.getWindow().getDecorView().performContextClick();
//    }

    @SuppressWarnings("deprecation")
    public static boolean checkIsOpen(String open, String close) {
        Date openDate = dateFromString(open, "HH:mm");
        Date closeDate = dateFromString(close, "HH:mm");
        Date current = Calendar.getInstance().getTime();
        int currentHour = current.getHours();
        int openHour = openDate.getHours();
        int closeHour = closeDate.getHours();
        int currentMinutes = current.getMinutes();
        int openMinutes = openDate.getMinutes();
        int closeMinutes = closeDate.getMinutes();
        if(currentHour>openHour && currentHour<closeHour)
            return true;
        else if(currentHour == openHour &&  currentMinutes >= openMinutes)
            return true;
        else if(currentHour == closeHour && currentMinutes < closeMinutes)
            return true;

        return false;
    }

    public static boolean isAccessibilityEnabled(Context context) {
        final AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return (accessibilityManager != null && accessibilityManager.isEnabled());
    }
}
