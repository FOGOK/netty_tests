import com.esotericsoftware.minlog.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.esotericsoftware.minlog.Log.*;

public class MyLogger extends Log.Logger{

    private long firstLogTime = System.currentTimeMillis();
    @Override
    public void log(int level, String category, String message, Throwable ex) {
        StringBuilder builder = new StringBuilder(256);

        long time = System.currentTimeMillis();
        long minutes = time / (1000 * 60);
        long seconds = time / (1000) % 60;
        long milliseconds = time % 1000;

        if (minutes <= 9) builder.append('0');
        builder.append(minutes);
        builder.append(':');
        if (seconds <= 9) builder.append('0');
        builder.append(seconds);
        builder.append(':');
        if (milliseconds <= 9) builder.append('0');
        if (milliseconds <= 99) builder.append('0');
        builder.append(milliseconds);


        switch (level) {
            case LEVEL_ERROR:
                builder.append(" ERROR: ");
                break;
            case LEVEL_WARN:
                builder.append("  WARN: ");
                break;
            case LEVEL_INFO:
                builder.append("  INFO: ");
                break;
            case LEVEL_DEBUG:
                builder.append(" DEBUG: ");
                break;
            case LEVEL_TRACE:
                builder.append(" TRACE: ");
                break;
        }

        if (category != null) {
            builder.append('[');
            builder.append(category);
            builder.append("] ");
        }

        builder.append(message);

        if (ex != null) {
            StringWriter writer = new StringWriter(256);
            ex.printStackTrace(new PrintWriter(writer));
            builder.append('\n');
            builder.append(writer.toString().trim());
        }

        print(builder.toString());
    }
}
