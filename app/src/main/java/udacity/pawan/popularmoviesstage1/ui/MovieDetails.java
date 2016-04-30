package udacity.pawan.popularmoviesstage1.ui;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import udacity.pawan.popularmoviesstage1.R;
import udacity.pawan.popularmoviesstage1.ui.fragments.MovieDetailsFragment;

public class MovieDetails extends AppCompatActivity {

    private final String LOG_TAG = MovieDetails.class.getSimpleName();

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MovieDetailsFragment()).commit();
        }
    }
}