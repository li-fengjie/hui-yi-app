package xyz.hui_yi.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import xyz.hui_yi.bean.AdmQianDaoBean;
import xyz.hui_yi.utils.image.SmartImageView;

import java.util.ArrayList;


/**
 * Created by LiFen on 2018/2/18.
 * 未完成签到人员列表adapter
 */

public class QianDaoNotOkAdapter extends BaseAdapter {

    ArrayList<AdmQianDaoBean.NotOk> result;
    Context context;

    public QianDaoNotOkAdapter(ArrayList<AdmQianDaoBean.NotOk> result, Context context) {
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
            convertView = View.inflate(context, xyz.hui_yi.R.layout.item_notok, null);
            //初始化holder对象，并初始化holder中的控件
            holder = new ViewHold();
            holder.tv_uname = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_notokname);
            holder.iv_uphoto = (SmartImageView) convertView.findViewById(xyz.hui_yi.R.id.iv_uphoto);
            holder.tv_uphone = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_notokphone);
            holder.tv_uid = (TextView)convertView.findViewById(xyz.hui_yi.R.id.tv_uid);

            //给当前view做个标记，并把数据存到该tag中
            convertView.setTag(holder);

        }else {
            //如果当前view存在，则直接从中取出其保存的控件及数据
            holder = (ViewHold) convertView.getTag();
        }
        //通过position获取当前item的Meeting数据，从Meeting数据中取出title、pubDate和image
        AdmQianDaoBean.NotOk notOk = result.get(position);
        holder.tv_uname.setText(notOk.uname);
        holder.tv_uphone.setText("tel:"+ notOk.uphone);
        holder.tv_uid.setText(notOk.uid);
        //使用SmartImageView的setImageUrl方法下载图片
        holder.iv_uphoto.setImageUrl(notOk.uphoto);
        return convertView;
    }

    /*
     * 用来存放item布局中控件的holder类
     */
    class ViewHold{
        SmartImageView iv_uphoto; //显示图片的控件，注意是SmartImageView
        TextView tv_uname; //显示姓名的控件
        TextView tv_uphone;
        TextView tv_uid;
    }
}