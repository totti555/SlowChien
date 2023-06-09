package com.example.slowchien.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentHomeBinding;
import com.example.slowchien.ui.exchange.ExchangeViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ViewPager tabHost;
    private final int[] tabIcons = {
            R.drawable.ic_email_24,
            R.drawable.ic_send_24,
            R.drawable.ic_question_24
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //HomeViewModel homeViewModel =
        //        new ViewModelProvider(this).get(HomeViewModel.class);
        //View root = binding.getRoot();

        TabLayout.Tab tab;

        View view = inflater.inflate(R.layout.fragment_home,container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager =  view.findViewById(R.id.view_pager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs =  view.findViewById(R.id.tabLayout);
        tabs.setupWithViewPager(viewPager);

        //set new message button text
        //final TextView textBtnNewMessage = binding.newMessage;
        //homeViewModel.getNewMessageBtnLib().observe(getViewLifecycleOwner(), textBtnNewMessage::setText);

        // Récupération de la référence au bouton de scan

        Button newMessageButton = (Button) view.findViewById(R.id.newMessageBtn);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Fragment fragment = new NewMessageFragment();
                //fragment.setArguments(args);


                Intent intent = new Intent(getActivity(), NewMessageActivity.class);
                intent.putExtra("pageName", "Nouveau Message");

                startActivity(intent);
            }
        });
/*
        RelativeLayout.LayoutParams newMessageButtonRelativeLayout =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //add rules
        newMessageButtonRelativeLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        newMessageButtonRelativeLayout.topMargin = 1000;

        newMessageButton.setLayoutParams(newMessageButtonRelativeLayout);
*/
        // Set icons of the tab bar
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setupWithViewPager(viewPager);

        for (int i=0; i<tabs.getTabCount();i++) {
            if(tabs.getTabAt(i) != null) {
                tabs.getTabAt(i).setIcon(tabIcons[i]);
            }
        }

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());

        SentFragment sentFragment = new SentFragment();
        Bundle sentArgs = new Bundle();
        sentArgs.putString("id", "Envoyés");
        sentFragment.setArguments(sentArgs);
        adapter.addFragment(new ReceiveFragment(), "Reçus");
        adapter.addFragment(new SentFragment(), "Envoyés");
        adapter.addFragment(new ChatFragment(), "Chat");


        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}