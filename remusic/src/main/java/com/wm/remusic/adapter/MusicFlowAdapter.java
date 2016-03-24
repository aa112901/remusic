package com.wm.remusic.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.R;

import java.util.List;

/**
 * Created by wm on 2016/2/21.
 */
public class MusicFlowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    List<OverFlowItem> mList;
    MusicInfo musicInfo;
    Activity mContext;

    public MusicFlowAdapter(Activity context, List<OverFlowItem> list, MusicInfo info) {
        mList = list;
        musicInfo = info;
        mContext = context;
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    //定义接口
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, String data);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (String) v.getTag());
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_window_listview, parent, false);
        ListItemViewHolder vh = new ListItemViewHolder(view);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        OverFlowItem minfo = mList.get(position);
        ((ListItemViewHolder) holder).icon.setImageResource(minfo.getAvatar());
        ((ListItemViewHolder) holder).title.setText(minfo.getTitle());
        //设置tag
        ((ListItemViewHolder) holder).itemView.setTag(position + "");

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        ListItemViewHolder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(R.id.pop_list_view);
            this.title = (TextView) view.findViewById(R.id.pop_list_item);

        }


    }

}
