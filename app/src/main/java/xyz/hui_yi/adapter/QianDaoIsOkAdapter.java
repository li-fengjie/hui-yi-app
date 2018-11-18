package xyz.hui_yi.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.hui_yi.R;
import xyz.hui_yi.bean.AdmQianDaoBean;
import xyz.hui_yi.utils.image.SmartImageView;
import xyz.hui_yi.utils.wifiutils.WifiUtils;


/**
 * Created by LiFen on 2018/2/18.
 * 签到成功人员adapter
 */

public class QianDaoIsOkAdapter extends BaseAdapter {

    ArrayList<AdmQianDaoBean.IsOk> result;
    Context context;

    public QianDaoIsOkAdapter(ArrayList<AdmQianDaoBean.IsOk> result, Context context) {
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
            convertView = View.inflate(context, R.layout.item_isok, null);
            //初始化holder对象，并初始化holder中的控件
            holder = new ViewHold();
            holder.tv_uname = (TextView) convertView.findViewById(R.id.tv_notokname);
            holder.tv_mtime = (TextView) convertView.findViewById(R.id.tv_isoktime);
            holder.iv_uphoto = (SmartImageView) convertView.findViewById(R.id.iv_uphoto);
            holder.tv_dis = (TextView)convertView.findViewById(R.id.tv_isokdistance);
            holder.iv_type = (ImageView)convertView.findViewById(R.id.iv_type);
            //给当前view做个标记，并把数据存到该tag中
            convertView.setTag(holder);

        }else {
            //如果当前view存在，则直接从中取出其保存的控件及数据
            holder = (ViewHold) convertView.getTag();
        }
        //通过position获取当前item的Meeting数据，从Meeting数据中取出title、pubDate和image
        AdmQianDaoBean.IsOk isOk = result.get(position);
        holder.tv_uname.setText(isOk.uname);
        String[] split = isOk.utime.split(" ");
        holder.tv_mtime.setText(split[1]);
        String dis = WifiUtils.disByRssi(Integer.parseInt(isOk.rssi));
        holder.tv_dis.setText("距离我:"+ dis + "米");
        String isfingerprint = isOk.isfingerprint;
        if(isfingerprint.equals("1")){
            holder.iv_type.setImageResource(R.mipmap.ic_fingerprint_black_36dp);
        }else {
            holder.iv_type.setImageResource(R.mipmap.ic_face);
        }
        //使用SmartImageView的setImageUrl方法下载图片
        holder.iv_uphoto.setImageUrl(isOk.uphoto);

        return convertView;
    }

    /*
     * 用来存放item布局中控件的holder类
     */
    class ViewHold{
        SmartImageView iv_uphoto; //显示图片的控件，注意是SmartImageView
        TextView tv_uname; //显示姓名的控件
        TextView tv_mtime;  //显示签到时间的控件
        TextView tv_dis;
        ImageView iv_type;//是否为指纹签到
    }
}