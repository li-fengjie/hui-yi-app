package xyz.hui_yi.adapter;

/**
 * Created by LiFen on 2018/2/10.
 * 会议展示列表adapter
 */


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import xyz.hui_yi.bean.MeetingsBean;
import xyz.hui_yi.utils.image.SmartImageView;

import java.util.ArrayList;


public class MeetingListAdapter extends BaseAdapter{

    ArrayList<MeetingsBean.Meeting> result;
    Context context;

    public MeetingListAdapter(ArrayList<MeetingsBean.Meeting> result, Context context) {
        super();
        this.result = result;
        this.context = context;
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return result.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //初始化holder对象
        ViewHold holder;
        if(convertView == null){
            //把条目布局转化为view对象
            convertView = View.inflate(context, xyz.hui_yi.R.layout.item, null);
            //初始化holder对象，并初始化holder中的控件
            holder = new ViewHold();
            holder.tv_mtitle = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_mtitle);
            holder.tv_mtime = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_mtime);
            holder.iv_icon = (SmartImageView) convertView.findViewById(xyz.hui_yi.R.id.iv_icon);
            holder.tv_mid = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_mid);
            holder.tv_maddress = (TextView)convertView.findViewById(xyz.hui_yi.R.id.tv_maddress);
            //给当前view做个标记，并把数据存到该tag中
            convertView.setTag(holder);

        }else {
            //如果当前view存在，则直接从中取出其保存的控件及数据
            holder = (ViewHold) convertView.getTag();
        }
        //通过position获取当前item的Meeting数据，从Meeting数据中取出title、pubDate和image
        MeetingsBean.Meeting Meeting = result.get(position);
        holder.tv_mtitle.setText(Meeting.mtitle);
        holder.tv_mtime.setText(Meeting.mlstartime);
        holder.tv_maddress.setText(Meeting.maddress);

        holder.tv_mid.setText(Meeting.mid);
        //使用SmartImageView的setImageUrl方法下载图片
        if(Meeting.mimage != null && !Meeting.mimage.equals("")){
            holder.iv_icon.setImageUrl(Meeting.mimage);
        }else {
            holder.iv_icon.setImageResource(xyz.hui_yi.R.drawable.image_huiyi_example);
        }
        return convertView;
    }

    /*
     * 用来存放item布局中控件的holder类
     */
    class ViewHold{
        SmartImageView iv_icon; //显示图片的控件，注意是SmartImageView
        TextView tv_mtitle; //显示标题的控件
        TextView tv_mtime;  //显示开始时间的控件
        TextView tv_mid;
        TextView tv_maddress;
    }
}