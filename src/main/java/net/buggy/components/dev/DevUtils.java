package net.buggy.components.dev;


import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Deprecated
/**
 * @deprecated Don't use this code in production
 */
public class DevUtils {

    public static void printLongException(Throwable throwable, boolean skipShort) {
        final StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));

        stringWriter.flush();

        final String className;
        if (throwable.getStackTrace().length > 0) {
            className = throwable.getStackTrace()[0].getClassName();
        } else {
            className = "UnknownClass";
        }

        final String stackTrace = stringWriter.toString();
        if (stackTrace.length() < 4000) {
            if (!skipShort) {
                Log.e(className, throwable.getMessage(), throwable);
            }
        } else {
            Log.e(className, throwable.getMessage());

            final String[] split = stackTrace.split("\n");
            int i = 0;
            StringBuilder buffer = new StringBuilder();
            while (i < split.length) {
                final String line = split[i];

                if (buffer.length() + line.length() > 4000) {
                    Log.e(className, buffer.toString());

                    buffer.delete(0, buffer.length());
                }

                buffer.append(line).append("\n");

                i++;
            }

            if (buffer.length() > 0) {
                Log.e(className, buffer.toString());
            }
        }

        try {
            stringWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
