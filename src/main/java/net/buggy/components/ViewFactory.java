package net.buggy.components;


import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

public class ViewFactory {

    public static TextView createTextView(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        try {
            return (TextView) inflater.getFactory().onCreateView("TextView", context, null);
        } catch (Exception e) {
            Log.w("ViewFactory", "createTextView: couldn't create a TextView via factory", e);
            return new AppCompatTextView(context);
        }
    }
}
