package com.wm.remusic.activity;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wm.remusic.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wm on 2016/3/16.
 */
public class MyViewPagerAdapter extends PagerAdapter { //显示的数据 private List<DataBean> datas = null;

    private LinkedList<View> mViewCache = null;
    private Context mContext;
    private LayoutInflater mLayoutInflater = null;

    public MyViewPagerAdapter(List datas, Context context) {
        super();
        //   this.datas = datas;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.mViewCache = new LinkedList<>();
    }

    @Override
    public int getCount() {
        //   Log.e("test", "getCount ");
        //  return this.datas.size();
        return 0;
    }

    @Override
    public int getItemPosition(Object object) {
        //    Log.e("test", "getItemPosition ");
        return super.getItemPosition(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //    Log.e("test", "instantiateItem " + position);
        ViewHolder viewHolder = null;
        View convertView = null;
        if (mViewCache.size() == 0) {
            convertView = this.mLayoutInflater.inflate(R.layout.layout_music, null, false);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.circle);
            FrameLayout frameLayout = (FrameLayout) convertView.findViewById(R.id.cc);
            viewHolder = new ViewHolder();
            viewHolder.imageView = imageView;
            viewHolder.frameLayout = frameLayout;
            convertView.setTag(viewHolder);
        } else {
            convertView = mViewCache.removeFirst();
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        viewHolder.textView.setTextColor(Color.YELLOW);
//        viewHolder.textView.setBackgroundColor(Color.GRAY);

        convertView.setTag(position);
        container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return convertView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //Log.e("test", "destroyItem " + position);
        View contentView = (View) object;
        container.removeView(contentView);
        this.mViewCache.add(contentView);
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        //  Log.e("test", "isViewFromObject ");
        return view == o;
    }

    public final class ViewHolder {
        public ImageView imageView;
        public FrameLayout frameLayout;
    }
}
