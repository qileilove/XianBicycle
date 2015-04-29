package org.weyoung.xianbicycle.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.weyoung.xianbicycle.R;
import org.weyoung.xianbicycle.data.BicycleData;
import org.weyoung.xianbicycle.net.Fetcher;
import org.weyoung.xianbicycle.net.Search;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import rx.functions.Action1;

public class SearchFragment extends Fragment{

    @InjectView(R.id.query)
    EditText query;
    @InjectView(R.id.result)
    ListView result;
    @InjectView(R.id.progress)
    ProgressBar progressBar;
    @InjectView(R.id.location)
    TextView locationHeader;

    private LocationClient locationClient;
    private LocationClientOption option;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        query.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    query();
                }
                return false;
            }
        });

        initLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        locationClient.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationClient.stop();
    }

    private void query() {
        showProgress();
        Fetcher fetcher = new Fetcher();
        Search search = new Search(query.getText().toString());
        fetcher.getData(search).subscribe(new Action1<List<BicycleData>>() {
            @Override
            public void call(List<BicycleData> data) {
                if (data.size() == 0) {
                    Toast.makeText(getActivity(), R.string.no_result, Toast.LENGTH_SHORT).show();
                } else {
                    result.setAdapter(new DataAdapter(getActivity(), data));
                }
                hideProgress();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                hideProgress();
            }
        });
    }

    @OnItemClick(R.id.result)
    void onResultItemClick(int index) {
    }

    @OnClick(R.id.location)
    void onLocationClick() {
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.requestLocation();
        }
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void initLocation() {
        option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);

        locationClient = new LocationClient(getActivity());
        locationClient.setLocOption(option);

        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null)
                    return;
                String format = String.format(Locale.US, getString(R.string.ur_location), location.getAddrStr());
                locationHeader.setText(format);
            }
        });
    }
}
