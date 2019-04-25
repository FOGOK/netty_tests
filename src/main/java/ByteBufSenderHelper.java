import com.esotericsoftware.kryo.io.ByteBufferOutput;

import static com.esotericsoftware.minlog.Log.info;

public class ByteBufSenderHelper {

    public static void reservePlaceToHeader(ByteBufferOutput output){
        output.writeInt(-1);
    }

    public static void setHeader(ByteBufferOutput output) {
        int lastPos = output.position();
        info("Set header: " + lastPos);
        output.setPosition(0);
        output.writeInt(lastPos);
        output.setPosition(lastPos);
    }

}
