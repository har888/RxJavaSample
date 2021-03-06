package com.example.harser777.rxjavasample;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.*;

import rx.*;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Subscription subscription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        subscription = getGistObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Gist>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                    @Override
                    public void onNext(Gist gist) {
                        //from onPostExecute()
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
                            sb.append(entry.getKey());
                            sb.append(" - ");
                            sb.append("Length of file");
                            sb.append(entry.getValue().content.length());
                            sb.append("\n");
                        }
                        TextView text = (TextView) findViewById(R.id.main_message);
                        text.setText(sb.toString());
                    }
                });

        }
/*
        new AsyncTask<Void, Void, Gist>() {
            public IOException error;


            @Override
            protected Gist doInBackground(Void... voids) {
                return getGist();
            }



            @Override
            protected void onPostExecute(Gist gist) {
                super.onPostExecute(gist);

                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
                    sb.append(entry.getKey());
                    sb.append(" - ");
                    sb.append("Length of file");
                    sb.append(entry.getValue().content.length());
                    sb.append("\n");
                }
                TextView text = (TextView) findViewById(R.id.main_message);
                text.setText(sb.toString());
            }


        }.execute();
    }*/


    @Nullable
    private Gist getGist() throws IOException {
        OkHttpClient client = new OkHttpClient();

        //from doInBackground
        Request request = new Request.Builder()
                .url("https://api.github.com/gists/db72a05cc03ef523ee74")
                .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Gist gist = new Gson().fromJson(response.body().charStream(), Gist.class);
                return gist;
            }
            return null;

    }

    public rx.Observable<Gist> getGistObservable(){
        return Observable.defer(new Func0<Observable<Gist>>() {
            @Override
            public Observable<Gist> call() {
                try {
                    return Observable.just(getGist());
                } catch (IOException e) {
                    return null;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(subscription !=null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }
}
