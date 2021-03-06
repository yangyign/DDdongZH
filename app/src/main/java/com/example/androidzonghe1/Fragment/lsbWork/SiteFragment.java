package com.example.androidzonghe1.Fragment.lsbWork;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.example.androidzonghe1.ConfigUtil;
import com.example.androidzonghe1.R;
import com.example.androidzonghe1.adapter.lsbWork.SiteAdapter;
import com.example.androidzonghe1.entity.lsbWork.CityEntity;
import com.example.androidzonghe1.entity.rjxWork.History;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class SiteFragment extends Fragment {
    RecyclerView listView;
    private SuggestionResult.SuggestionInfo info;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("SiteFragment", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_site, container, false);
        listView = view.findViewById(R.id.list_site);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(linearLayoutManager);
        initListView();
        return view;
    }
    public void initListView(){
        Log.e("SiteFragment", "initListView");
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getParcelable("suggestionResult") != null){
            List<SuggestionResult.SuggestionInfo> suggestionInfos = ((SuggestionResult)bundle.getParcelable("suggestionResult")).getAllSuggestions();;
            SiteAdapter siteAdapter = new SiteAdapter(suggestionInfos);
            siteAdapter.setOnItemClickListener((parent, view, position, data) -> {
                Intent response = new Intent();
                Bundle b = new Bundle();
                info = data;
                b.putParcelable("suggestionInfo", data);
                response.putExtras(b);
                addUsePosition();
                uploadHistory(ConfigUtil.xt+"AddHistoryServlet");
                getActivity().setResult(0, response);
                getActivity().finish();
            });
            listView.setAdapter(siteAdapter);
        }
    }

    //??????????????????????????????????????????
    private void uploadHistory(final String str){
        new Thread(){
            @Override
            public void run() {
                super.run();
                //????????????????????????????????????????????????????????????
                History history = new History();
                history.setUserPhone("123456");//?????????
                history.setCity(info.city);
                history.setKey(info.key);
                history.setLatitude(info.getPt().latitude);
                history.setLongitude(info.getPt().longitude);
                try {
                    Gson gson = new Gson();
                    String jsonObject = gson.toJson(history);
                    URL url = new URL(str);
                    //????????????????????????URLConnection
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonObject.getBytes());
                    os.flush();
                    //?????????????????????
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                    String addFlag = reader.readLine();
                    System.out.println(addFlag);
                    is.close();
                    os.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //??????????????????
    public void addUsePosition(){
        //???????????????
        SuggestionResult.SuggestionInfo suggestionInfo = new SuggestionResult.SuggestionInfo();
        suggestionInfo.setKey(info.key);
        suggestionInfo.setCity(info.city);
//        suggestionInfo.setDistrict("?????????");
        suggestionInfo.setPt(new LatLng(info.pt.latitude, info.getPt().longitude));
//        suggestionInfo.setUid("42362707d679c71f5cbe86c3");
//        suggestionInfo.setTag("??????");
//        suggestionInfo.setAddress("????????????-?????????-???????????????20???");
        suggestionInfo.setPoiChildrenInfoList(null);
        CityFragment.hotCityList.add(suggestionInfo);
    }
}