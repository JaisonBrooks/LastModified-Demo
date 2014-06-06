package com.jaisonbrooks.github.setfilemoddate.app;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity {

    String URL = "http://textfiles.com/art/sunlogo.txt";
    TextView tv_fileLocation;
    TextView tv_currentDate;
    TextView tv_lastMod;
    File txt_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        findViewByIds();
        checkForFile();
    }

    private void checkForFile() {

        txt_file = new File(Environment.getExternalStorageDirectory(), "/data/SetLastModDate/sunlogo.txt");
        if (!txt_file.exists()) {
            new DownloadTask(this, URL).execute();
        } else {
            tv_fileLocation.setText(txt_file.getAbsolutePath());
            tv_currentDate.setText(dateString());
            tv_lastMod.setText(Long.toString(txt_file.lastModified()));
        }
    }

    public String dateString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public void findViewByIds() {
        tv_fileLocation = (TextView) findViewById(R.id.tv_file_location);
        tv_currentDate = (TextView) findViewById(R.id.tv_current_date);
        tv_lastMod = (TextView) findViewById(R.id.tv_last_mod);

        Button btn_change_date = (Button) findViewById(R.id.btn_change_mod);
        btn_change_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                System.out.println("Original Last Modified Date : "
                        + dateFormat.format(txt_file.lastModified()));

                // Create a calendar object that will convert the date and time value in milliseconds to date.
                Calendar calendar = Calendar.getInstance();
                long milliSeconds = calendar.getTimeInMillis();
                calendar.setTimeInMillis(milliSeconds);

                // we have to convert the above date to milliseconds...
               // Date newLastModifiedDate
                txt_file.setLastModified(milliSeconds);

            }
        });

    }

    public void setFileModified(File file, long modifiedDate) {

        file.setLastModified(modifiedDate);

    };

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private String URL;

        public DownloadTask(Context context, String URL) {
            this.context = context;
            this.URL = URL;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();


                File targetDir = new File(
                        Environment.getExternalStorageDirectory(),
                        "/data/SetLastModDate"); //Create .hidden folder inside /data folder
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                txt_file = new File(targetDir, "sunlogo" + ".txt");

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(txt_file);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setProgressBarIndeterminateVisibility(false);
            checkForFile();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
