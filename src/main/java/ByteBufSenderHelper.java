import com.esotericsoftware.kryo.io.ByteBufferOutput;

public class ByteBufSenderHelper {

    public static void reservePlaceToHeader(ByteBufferOutput output){
        output.writeInt(-1);
    }

    public static void setHeader(ByteBufferOutput output) {
        int lastPos = output.position();
        output.setPosition(0);
        output.writeInt(lastPos);
        output.setPosition(lastPos);
    }

}
