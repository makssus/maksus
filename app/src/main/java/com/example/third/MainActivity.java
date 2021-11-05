package com.example.third;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // Поля, что будут ссылаться на объекты из дизайна
    private EditText user_field;
    private TextView result_info;
    private TextView description_info;
    private TextView city_info;
    private RelativeLayout main_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Сработает при создании Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScrollView constraintLayout = findViewById(R.id.main_layout);


        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();



        // Устанавливаем ссылки на объекты из дизайна
        user_field = findViewById(R.id.user_field);
        Button main_btn = findViewById(R.id.main_btn);
        Button act_change = findViewById(R.id.act_change);
        result_info = findViewById(R.id.result_info);
        description_info = findViewById(R.id.description_info);
        city_info = findViewById(R.id.city_info);

        // Обработчик нажатия на кнопку
        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {



                    // Если ничего не ввели в поле, то выдаем всплывающую подсказку
                    if (user_field.getText().toString().trim().equals("")) {
                        Toast.makeText(MainActivity.this, R.string.no_user_input, Toast.LENGTH_LONG).show();
                    }

                    else {
                        // Если ввели, то формируем ссылку для получения погоды
                        String city = user_field.getText().toString();
                        String key = "255b1603c0e7f141aa2211b89cdd5d63";
                        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";

                        // Запускаем класс для получения погоды
                        new GetURLData().execute(url);
                        user_field.setText("");
                        hideKeyboard();


                    }

                }
                else{Toast.makeText(MainActivity.this, R.string.InternetCon, Toast.LENGTH_LONG).show();}
            }
        });
        act_change.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(".SecondActivity");
                        startActivity(intent);
                    }
                }
        );
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    @SuppressLint("StaticFieldLeak")
    private class GetURLData extends AsyncTask<String, String, String> {

        // Будет выполнено до отправки данных по URL
        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Ожидайте...");
            description_info.setText("");
            city_info.setText("");
        }

        // Будет выполняться во время подключения по URL
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Создаем URL подключение, а также HTTP подключение
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int code = connection.getResponseCode();
                if (code == 404) {
                    result_info.setText("Город не найден...");
                    return "false";
                }
                // Создаем объекты для считывания данных из файла
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                // Генерируемая строка
                StringBuilder buffer = new StringBuilder();
                String line = "";

                // Считываем файл и записываем все в строку
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");



                // Возвращаем строку
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Закрываем соединения
                if (connection != null)
                    connection.disconnect();

                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        // Выполняется после завершения получения данных
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Конвертируем JSON формат и выводим данные в текстовом поле
            try {
                JSONObject jsonObject = new JSONObject(result);
                city_info.setText("Город "+ jsonObject.getString("name") + ":\n" + jsonObject.getJSONArray("weather").getJSONObject(0).getString("description"));
                result_info.setText("Температура: " + jsonObject.getJSONObject("main").getDouble("temp"));
                description_info.setText("Скорость ветра: " + jsonObject.getJSONObject("wind").getDouble("speed"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}