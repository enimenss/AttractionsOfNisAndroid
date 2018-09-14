package com.example.a38162.attractionsofnis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendListViewAdapter extends ArrayAdapter<String> {

    private List<String> friendList;
    private Context context;

    public FriendListViewAdapter(List<String> friendList, Context context) {
        super(context, R.layout.friend_list_item, friendList);
        this.friendList = friendList;
        this.context = context;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View FriendListViewItem = inflater.inflate(R.layout.friend_list_item, null, true);

        //TextView rank = (TextView) listViewItem.findViewById(R.id.textViewRankListRank);
        ImageView image = (ImageView) FriendListViewItem.findViewById(R.id.imageViewFriendListImage);
        TextView firstname =(TextView) FriendListViewItem.findViewById(R.id.textViewFriendListFirstName);
        TextView lastname =(TextView) FriendListViewItem.findViewById(R.id.textViewFriendListLastName);
        //TextView checkins = (TextView)  listViewItem.findViewById(R.id.textViewRankListCheckins);

       /* String friend = friendList.get(position);
        int id = UsersData.getInstance().usersKeyIndexMapping.get(friend);
        User user = UsersData.getInstance().getUser(id);


        //rank.setText(String.valueOf(Users.getInstance().usersKeyIndexMapping.get(user.key)+1));

        if(!user.picture.isEmpty())
        {
            Picasso.with(context).load(user.picture).into(image);
        }

        firstname.setText(user.name);
        lastname.setText(user.surname);*/
        //checkins.setText(String.valueOf(1000000000 - user.checkins));



        return FriendListViewItem;
    }
}
