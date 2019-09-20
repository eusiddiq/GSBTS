package com.team17.gsbts.Services;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseNotificationHelper {


    final String TAG="SnapshotFire";
    private final String SERVER_KEY ="AAAAqnJKtjQ:APA91bGkERKzG99aqj-uSij_AFnx9QZ6OT0q0sgVdF4ymCGiHz4vYUY5nx3_4TUfD4taUq64kd2ecIS485kWziD67TK4OIWT3OL4Ix7AjcM8mCKSyJOikZgeVKdXJPk09SxxE-RgZM_7";
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    OkHttpClient mClient = new OkHttpClient();

    public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    Log.d(TAG, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Log.d(TAG,"Message Success: " + success);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG,"Message Failed: ");
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

}
