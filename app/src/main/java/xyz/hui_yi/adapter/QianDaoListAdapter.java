package xyz.hui_yi.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.hui_yi.bean.QianDaosBean;
import xyz.hui_yi.utils.image.SmartImageView;

import java.util.ArrayList;


/**
 * Created by LiFen on 2018/2/16.
 * 将要举行会议签到展示界面
 */

public class QianDaoListAdapter extends BaseAdapter{
    private static final String TAG = "QianDaoListAdapter";
    ArrayList<QianDaosBean.QianDaos> result;
    Context context;

    public QianDaoListAdapter(ArrayList<QianDaosBean.QianDaos> result, Context context) {
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
            convertView = View.inflate(context, xyz.hui_yi.R.layout.item2, null);
            //初始化holder对象，并初始化holder中的控件
            holder = new ViewHold();
            holder.tv_mtitle = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_mtitle);
            holder.tv_mtime = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_mtime);
            holder.iv_uphoto = (SmartImageView) convertView.findViewById(xyz.hui_yi.R.id.iv_uphoto);
            holder.tv_mid = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_mid);
            holder.tv_maddress = (TextView)convertView.findViewById(xyz.hui_yi.R.id.tv_maddress);
            holder.tv_wlanmac = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_wlanmac);
            holder.tv_bluetoothmac = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_bluetoothmac);
            holder.iv_type = (ImageView) convertView.findViewById(xyz.hui_yi.R.id.iv_type);
            holder.tv_sign_id = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_sign_id);
            //给当前view做个标记，并把数据存到该tag中
            convertView.setTag(holder);

        }else {
            //如果当前view存在，则直接从中取出其保存的控件及数据
            holder = (ViewHold) convertView.getTag();
        }
        //通过position获取当前item的Meeting数据，从Meeting数据中取出title、pubDate和image
        QianDaosBean.QianDaos qianDaos = result.get(position);
        holder.tv_mtitle.setText(qianDaos.mtitle);
        /*
        对时间进行格式化
         */
        String[] start = qianDaos.mlstartime.split(" ");
        String[] end = qianDaos.mlendtime.split(" ");
        String startDate = start[0];
        String startTime = start[1];
        String endDate = end[0];
        String endTime = end[1];
        if(startDate.equals(endDate)){
            holder.tv_mtime.setText(startDate.substring(2) + " " +startTime + "～" +endTime);
        }else {
            holder.tv_mtime.setText(startDate.substring(2) + " " + startTime
                    + "～"+ endDate.substring(2) + " " +endTime);
        }
        holder.tv_maddress.setText(qianDaos.maddress);
        holder.tv_mid.setText(qianDaos.mid);
        holder.tv_wlanmac.setText(qianDaos.wlanmac);
        holder.tv_bluetoothmac.setText(qianDaos.bluetoothmac);
        Log.i(TAG, "getView: " + qianDaos.issign);
        if(qianDaos.issign == null){//管理员身份
            if(qianDaos.wlanmac.equals("") && !qianDaos.bluetoothmac.equals("")){
                holder.iv_type.setImageResource(xyz.hui_yi.R.mipmap.ic_type_bluetooth);
            }else {
                holder.iv_type.setImageResource(xyz.hui_yi.R.mipmap.ic_type_wifi);
            }
        }else {//用户
            holder.tv_sign_id.setText(qianDaos.sign_id);
            if(qianDaos.issign.equals("0")){//未签到
                if(qianDaos.wlanmac.equals("")
                        && !qianDaos.bluetoothmac.equals("")){
                    holder.iv_type.setImageResource(xyz.hui_yi.R.mipmap.ic_type_bluetooth);
                }else if(!qianDaos.wlanmac.equals("")
                        && qianDaos.bluetoothmac.equals("")){
                    holder.iv_type.setImageResource(xyz.hui_yi.R.mipmap.ic_type_wifi);
                }
            }else {//已签到
                holder.iv_type.setImageResource(xyz.hui_yi.R.mipmap.ic_qiandao_ok);
            }
        }
        //使用SmartImageView的setImageUrl方法下载图片
        if(!qianDaos.uphoto.equals("")){
            holder.iv_uphoto.setImageUrl(qianDaos.uphoto);
        }else {
            holder.iv_uphoto.setImageResource(xyz.hui_yi.R.drawable.image_uphoto_example);
        }
        return convertView;
    }

    /*
     * 用来存放item布局中控件的holder类
     */
    class ViewHold{
        SmartImageView iv_uphoto; //显示图片的控件，注意是SmartImageView
        ImageView iv_type;
        TextView tv_mtitle; //显示标题的控件
        TextView tv_mtime;  //显示开始时间的控件
        TextView tv_mid;
        TextView tv_maddress;
        TextView tv_wlanmac;
        TextView tv_bluetoothmac;
        TextView tv_sign_id;
    }
}
