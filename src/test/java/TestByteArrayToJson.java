import Serializer.Serializer;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by derekhsieh on 3/28/16.
 */
public class TestByteArrayToJson {
    public static void main(String[] args){
        String test = "This is a test, to see if byte array to json creates special characters";
        byte[] testToBytes = Serializer.toByteArray(test);

        Gson gson = new Gson();
        String convert = gson.toJson(testToBytes);
        System.out.println(convert);

        byte[] convertedBack = gson.fromJson(convert, byte[].class);
        String shouldBeTest = (String) Serializer.toObject(convertedBack);
        System.out.println(shouldBeTest);
    }
}
