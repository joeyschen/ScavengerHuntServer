import java.util.Arrays;

/**
 * Created by derekhsieh on 10/7/15.
 */
public class TestStringSplit {

    public static void main(String[] args){
        String toSplit = "username=user&password=pass";
        String[] split = toSplit.split("\\&");
        System.out.println(Arrays.toString(split));
    }
}
