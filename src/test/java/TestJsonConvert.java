import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by derekhsieh on 12/30/15.
 */
public class TestJsonConvert {
    public static void main(String[] args){
        Gson gson = new Gson();
        ArrayList<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");
        list.add("third");

        String converted = gson.toJson(list);
        System.out.println(converted);
        ArrayList<String> convertedBack = gson.fromJson(converted, ArrayList.class);
        for(int i = 0; i < convertedBack.size(); i++){
            System.out.println(convertedBack.get(i));
        }
    }
}
