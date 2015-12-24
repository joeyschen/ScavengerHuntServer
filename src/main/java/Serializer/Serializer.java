package Serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by derekhsieh on 10/8/15.
 */


/*
Serializer class that will do the serilazation and deserialization
 */
public class Serializer {

    private static Logger logger = Logger.getLogger(Serializer.class);
    private static Gson gson = new Gson();

    public Serializer() {

    }

    public static byte[] toByteArray(Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output serializer = new Hessian2Output(outputStream);
        try {
            serializer.writeObject(object);
            serializer.flushBuffer();
            serializer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return outputStream.toByteArray();
    }

    public static Object toObject(byte[] byteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        Hessian2Input deserializer = new Hessian2Input();
        try {
            return deserializer.readObject();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                deserializer.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}
