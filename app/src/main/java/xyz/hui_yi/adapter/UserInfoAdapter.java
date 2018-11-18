package xyz.hui_yi.adapter;

/**
 * Created by LiFen on 2018/2/2.
 * 添加参会人员列表adapter
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import xyz.hui_yi.bean.UsersInforBean;
import xyz.hui_yi.utils.image.SmartImageView;

import java.util.ArrayList;
import java.util.HashMap;


public class UserInfoAdapter extends BaseAdapter {
    // 填充数据的list
    ArrayList<UsersInforBean.UserInfo> result;
    HashMap<Integer, Boolean> isSelected;

    // 上下文
    private Context context;

    // 用来导入布局
    private LayoutInflater inflater = null;


    // 构造器
    public UserInfoAdapter(ArrayList<UsersInforBean.UserInfo> result, Context context,HashMap<Integer, Boolean> isSelected) {
        this.context = context;
        this.result = result;
        this.isSelected = isSelected;
        inflater = LayoutInflater.from(context);
        // 初始化数据
        initDate();
    }

    // 初始化isSelected的数据
    private void initDate() {
        for (int i = 0; i < result.size(); i++) {
            isSelected.put(i, false);
        }
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
        ViewHolder holder = null;
        if (convertView == null) {
            // 获得ViewHolder对象
            holder = new ViewHolder();
            // 导入布局并赋值给convertview
            convertView = inflater.inflate(xyz.hui_yi.R.layout.item_userinfor, null);
            holder.tv_uname = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_uname);
            holder.cb = (CheckBox) convertView.findViewById(xyz.hui_yi.R.id.item_cb);
            holder.iv_uphoto = (SmartImageView) convertView.findViewById(xyz.hui_yi.R.id.iv_uphoto);
            holder.tv_uphone = (TextView) convertView.findViewById(xyz.hui_yi.R.id.tv_tel);
            // 为view设置标签
            convertView.setTag(holder);
        } else {
            // 取出holder
            holder = (ViewHolder) convertView.getTag();
        }
        //通过position获取当前item的Meeting数据，从Meeting数据中取出title、pubDate和image
        UsersInforBean.UserInfo userInfo = result.get(position);
        // 设置list中TextView的显示
        holder.tv_uname.setText(userInfo.uname);
        //使用SmartImageView的setImageUrl方法下载图片
        holder.iv_uphoto.setImageUrl(userInfo.uphoto);
        // 根据isSelected来设置checkbox的选中状况
        holder.cb.setChecked(isSelected.get(position));
        holder.tv_uphone.setText(userInfo.uid);
        return convertView;
    }


    public static class ViewHolder {
        public SmartImageView iv_uphoto;
        public TextView tv_uname;
        public CheckBox cb;
        public TextView tv_uphone;
    }
}