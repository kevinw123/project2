package elec291group2.com.project2;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Kevin on 2016-03-23.
 */
public class Overview extends Fragment
{
    View view;
    TextView overview;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.overview, container, false);
        overview = (TextView) view.findViewById(R.id.overview);
        overview.setText("test");

        return view;
    }
}
