package com.lifeshare.ui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.lifeshare.BaseFragment;
import com.lifeshare.R;
import com.lifeshare.ui.inviteFriends.InviteViaMobileActivity;
import com.lifeshare.ui.my_connection.AddMyConnectionListActivity;
import com.lifeshare.utils.CustomViewPager;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllPendingConnectionFragment extends BaseFragment implements View.OnClickListener {

    private CustomViewPager viewPager;
    private AppCompatTextView tvPendingConnection, tvAllConnection;
    private CircleImageView ivAdd;
    private RelativeLayout appBar;
    private AppCompatTextView tvToolbarTitle, tvDone;
    private MyAdapter adapter;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_all_pending_connection, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {

        appBar = (RelativeLayout) rootView.findViewById(R.id.appbar_new);
        tvToolbarTitle = (AppCompatTextView) appBar.findViewById(R.id.tvToolbarTitle);
        tvDone = (AppCompatTextView) appBar.findViewById(R.id.tvDone);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(R.string.connection);
        tvDone.setText(R.string.invite);
        tvDone.setOnClickListener(this);
        ivAdd = (CircleImageView) appBar.findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(this);
        tvDone.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);

        viewPager = (CustomViewPager) rootView.findViewById(R.id.viewPager);
        tvAllConnection = (AppCompatTextView) rootView.findViewById(R.id.tvAllConnection);
        tvPendingConnection = (AppCompatTextView) rootView.findViewById(R.id.tvPendingConnection);
        tvAllConnection.setOnClickListener(this);
        tvPendingConnection.setOnClickListener(this);
        viewPager.setPagingEnabled(false);
        adapter = new MyAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tvAllConnection.setTextAppearance(requireContext(), R.style.TabStyleMatchSel);
                    tvPendingConnection.setTextAppearance(requireContext(), R.style.TabStyleMatchNotSel);
                    tvAllConnection.setBackgroundResource(R.drawable.shape_tab_white_sel);
                    tvPendingConnection.setBackgroundResource(0);
                    tvDone.setVisibility(View.GONE);
                    ivAdd.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    tvAllConnection.setTextAppearance(requireContext(), R.style.TabStyleMatchNotSel);
                    tvPendingConnection.setTextAppearance(requireContext(), R.style.TabStyleMatchSel);
                    tvAllConnection.setBackgroundResource(0);
                    tvPendingConnection.setBackgroundResource(R.drawable.shape_tab_white_sel);
                    tvDone.setVisibility(View.VISIBLE);
                    ivAdd.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvAllConnection:
                viewPager.setCurrentItem(0);
                break;
            case R.id.tvPendingConnection:
                viewPager.setCurrentItem(1);
                break;
            case R.id.ivAdd:
                startActivity(new Intent(requireContext(), AddMyConnectionListActivity.class));
                break;
            case R.id.tvDone:
                startActivity(new Intent(requireContext(), InviteViaMobileActivity.class));
                break;
        }
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return new RequestsFragment();
            } else {
                return new ConnectionFragment();
            }
        }
    }


}