package udacity.pawan.popularmoviesstage1.ui.fragments;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import udacity.pawan.popularmoviesstage1.R;
import udacity.pawan.popularmoviesstage1.controller.ApiManager;
import udacity.pawan.popularmoviesstage1.model.adapter.ReviewAdapter;
import udacity.pawan.popularmoviesstage1.model.adapter.TrailerAdapter;
import udacity.pawan.popularmoviesstage1.model.pojos.Database;
import udacity.pawan.popularmoviesstage1.model.pojos.Result;
import udacity.pawan.popularmoviesstage1.model.pojos.Reviews;
import udacity.pawan.popularmoviesstage1.model.pojos.Trailers;

/**
 * Created by pa1pal on 27/4/16.
 */
public class MovieDetailsFragment extends Fragment {
    public static final int TYPE_LINEAR_LAYOUT = 1;
    private final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private Result mMovieDetails;

    TrailerAdapter mTrailerAdapter;
    ReviewAdapter mReviewAdapter;
    private ApiManager apiManager;
    private int mMovieId;
    private Trailers mTrailers;
    private Reviews mReviews;
    private TextView mTrailerHeaderTextView;
    private TextView mReviewHeaderTextView;
    private ArrayList<Trailers> trailerList;
    private ArrayList<Reviews> reviewList;

    @Bind(R.id.iv_image) ImageView mPosterImage;
    @Bind(R.id.tv_title) TextView mTitle;
    @Bind(R.id.plot) TextView mPlot;
    @Bind(R.id.rating) TextView mRating;
    @Bind(R.id.rdate) TextView mReleaseDate;

    @Bind(R.id.addtofavourite)
    Button favouriteButton;
    @Bind(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    RecyclerView mTrailersList;
    RecyclerView mReviewsList;
    private int type = TYPE_LINEAR_LAYOUT;

    public MovieDetailsFragment(Result result, int mid){
        mMovieDetails = result;
        mMovieId = mid;
    }
    public MovieDetailsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type", TYPE_LINEAR_LAYOUT);
        }


        mTrailers = new Trailers();
        mReviews = new Reviews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_movie_details, container, false);


        mTrailersList = (RecyclerView) rootview.findViewById(R.id.trailersList);
        mReviewsList = (RecyclerView) rootview.findViewById(R.id.reviewsList);

        ButterKnife.bind(this, rootview);

//        mTrailerAdapter = new TrailerAdapter(getActivity(),mTrailers.getVideoResults());
//        mReviewAdapter = new ReviewAdapter(getActivity(), mReviews.getReviewsResults());

        Picasso.with(getActivity())
                .load("http://image.tmdb.org/t/p/w500" + mMovieDetails.getPosterPath())
                .into(mPosterImage);

        mTitle.setText(mMovieDetails.getOriginalTitle());
        mPlot.setText("Overview \n" + mMovieDetails.getOverview());
        mRating.setText("User Rating : " + mMovieDetails.getVoteAverage());
        mReleaseDate.setText("Release Date : " + mMovieDetails.getReleaseDate());

        Select Query = Select.from(Database.class)
                .where(Condition.prop("id_Movie_Result").eq(mMovieDetails.getId()));
        long numberQuery = Query.count();

        if (numberQuery == 0) {
            favouriteButton.setText("Add to database");
        } else {
            favouriteButton.setText("Remove from database");
        }


        return  rootview;
    }


    @OnClick(R.id.addtofavourite)

    public void saveToDB(){
        Select Query = Select.from(Database.class)
                .where(Condition.prop("id_Movie_Result").eq(
                        mMovieDetails.getId())).limit("1");
        long numberQuery = Query.count();

        if (numberQuery == 0) {
            Database movieResult = new Database(
                    mMovieDetails.getPosterPath(),
                    mMovieDetails.getAdult(),
                    mMovieDetails.getOverview(),
                    mMovieDetails.getReleaseDate(),
                    mMovieDetails.getGenreIds(),
                    mMovieDetails.getId(),
                    mMovieDetails.getOriginalTitle(),
                    mMovieDetails.getOriginalLanguage(),
                    mMovieDetails.getTitle(),
                    mMovieDetails.getBackdropPath(),
                    mMovieDetails.getPopularity(),
                    mMovieDetails.getVoteCount(),
                    mMovieDetails.getVideo(),
                    mMovieDetails.getVoteAverage());
            movieResult.save();
            favouriteButton.setText("Remove from database");
        } else {
            Database.deleteAll(Database.class, "id_Movie_Result = ?", mMovieDetails.getId()
                    +"");
            favouriteButton.setText("Add to database");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTrailersList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReviewsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadReviews();
        loadTrailers();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    public void loadTrailers() {
        apiManager = new ApiManager();
        Call<Trailers> trailerCall = apiManager.getMovieService().getTrailers(mMovieId);
        trailerCall.enqueue(new Callback<Trailers>() {
            @Override
            public void onResponse(Call<Trailers> call, Response<Trailers> response) {
                if(response.isSuccessful()){
                    mTrailers = response.body();
                    mTrailerAdapter = new TrailerAdapter(getActivity(),mTrailers.getVideoResults());
                    mTrailerAdapter.notifyDataSetChanged();
                    // TODO : setAdapter not working
                    mTrailersList.setAdapter(mTrailerAdapter);
                    //Log.d(LOG_TAG,mPopularMovies.getResults().get(2).getOriginalTitle());
                }
            }

            @Override
            public void onFailure(Call<Trailers> call, Throwable t) {

            }
        });
    }


    public void loadReviews() {
        apiManager = new ApiManager();
        Call<Reviews> reviewsCall = apiManager.getMovieService().getReviews(mMovieId);
        reviewsCall.enqueue(new Callback<Reviews>() {
            @Override
            public void onResponse(Call<Reviews> call, Response<Reviews> response) {
                if(response.isSuccessful()){
                    mReviews = response.body();
                    mReviewAdapter = new ReviewAdapter(getActivity(), mReviews.getReviewsResults());
                    mReviewAdapter.notifyDataSetChanged();
                    // TODO : setAdapter not working
                    mReviewsList.setAdapter(mReviewAdapter);
                    //Log.d(LOG_TAG,mPopularMovies.getResults().get(2).getOriginalTitle());
                }
            }

            @Override
            public void onFailure(Call<Reviews> call, Throwable t) {

            }

        });
    }

    // TODO : Trailer onClickListner

}