import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by derekhsieh on 4/3/16.
 */
public class TestPost {


    @Test
    public void testPost() throws IOException {
         HttpClient client;
         HttpPost post;

        client = new DefaultHttpClient();
        post = new HttpPost("http://192.168.0.103:4567/LoginRequest");

        List<NameValuePair> toPost = new ArrayList<>();
        toPost.add(new BasicNameValuePair("username", "test"));
        toPost.add(new BasicNameValuePair("password", "who"));
        post.setEntity(new UrlEncodedFormEntity(toPost));
        HttpResponse response = client.execute(post);

        System.out.println("Here");
    }


}
