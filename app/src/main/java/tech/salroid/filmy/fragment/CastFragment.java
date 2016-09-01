package tech.salroid.filmy.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.salroid.filmy.R;
import tech.salroid.filmy.activities.CharacterDetailsActivity;
import tech.salroid.filmy.activities.FullCastActivity;
import tech.salroid.filmy.custom_adapter.MovieDetailsActivityAdapter;
import tech.salroid.filmy.customs.BreathingProgress;
import tech.salroid.filmy.data_classes.MovieDetailsData;
import tech.salroid.filmy.network_stuff.VolleySingleton;
import tech.salroid.filmy.parser.MovieDetailsActivityParseWork;
/*
 * Filmy Application for Android
 * Copyright (c) 2016 Ramankit Singh (http://github.com/webianks).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class CastFragment extends Fragment implements View.OnClickListener, MovieDetailsActivityAdapter.ClickListener {


    private String cast_json;
    private String movieId, movieTitle;

    @BindView(R.id.more)
    TextView more;
    @BindView(R.id.cast_recycler)
    RecyclerView cast_recycler;
    @BindView(R.id.card_holder)
    TextView card_holder;
    @BindView(R.id.breathingProgressFragment)
    BreathingProgress breathingProgress;

    public static CastFragment newInstance(String movie_Id, String movie_Title) {
        CastFragment fragment = new CastFragment();
        Bundle args = new Bundle();
        args.putString("movie_id", movie_Id);
        args.putString("movie_title", movie_Title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cast_fragment, container, false);
        ButterKnife.bind(this, view);

        cast_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        cast_recycler.setNestedScrollingEnabled(false);

        cast_recycler.setVisibility(View.INVISIBLE);

        more.setOnClickListener(this);


        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle savedBundle = getArguments();

        if (savedBundle != null) {

            movieId = savedBundle.getString("movie_id");
            movieTitle = savedBundle.getString("movie_title");

        }


        if (movieId != null)
            getCastFromNetwork(movieId);

    }


    public void getCastFromNetwork(String movieId) {


        final String BASE_MOVIE_CAST_DETAILS = new String("https://api.trakt.tv/movies/" + movieId + "/people?extended=images");
        JsonObjectRequest jsonObjectRequestForMovieCastDetails = new JsonObjectRequest(Request.Method.GET, BASE_MOVIE_CAST_DETAILS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        cast_json = response.toString();
                        cast_parseOutput(response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("webi", "Volley Error: " + error.getCause());

                breathingProgress.setVisibility(View.GONE);

            }
        }
        );


        VolleySingleton volleySingleton = VolleySingleton.getInstance();
        RequestQueue requestQueue = volleySingleton.getRequestQueue();
        requestQueue.add(jsonObjectRequestForMovieCastDetails);
    }


    private void cast_parseOutput(String cast_result) {

        MovieDetailsActivityParseWork par = new MovieDetailsActivityParseWork(getActivity(), cast_result);
        List<MovieDetailsData> cast_list = par.parse_cast();
        MovieDetailsActivityAdapter cast_adapter = new MovieDetailsActivityAdapter(getActivity(), cast_list, true);
        cast_adapter.setClickListener(this);
        cast_recycler.setAdapter(cast_adapter);
        if (cast_list.size() > 4)
            more.setVisibility(View.VISIBLE);
        else if (cast_list.size() == 0) {
            more.setVisibility(View.INVISIBLE);
            card_holder.setVisibility(View.INVISIBLE);
        } else
            more.setVisibility(View.INVISIBLE);

        breathingProgress.setVisibility(View.GONE);
        cast_recycler.setVisibility(View.VISIBLE);


    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.more) {

            Log.d("webi",""+movieTitle);
            if (cast_json != null && movieTitle != null) {


                Intent intent = new Intent(getActivity(), FullCastActivity.class);
                intent.putExtra("cast_json", cast_json);
                intent.putExtra("toolbar_title", movieTitle);
                startActivity(intent);

            }
        }


    }

    @Override
    public void itemClicked(MovieDetailsData setterGetter, int position, View view) {
        Intent intent = new Intent(getActivity(), CharacterDetailsActivity.class);
        intent.putExtra("id", setterGetter.getCast_id());

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {

            Pair<View, String> p1 = Pair.create(view.findViewById(R.id.cast_poster), "profile");
            Pair<View, String> p2 = Pair.create(view.findViewById(R.id.cast_name), "name");

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), p1, p2);
            startActivity(intent, options.toBundle());

        } else {
            startActivity(intent);
        }

    }

}