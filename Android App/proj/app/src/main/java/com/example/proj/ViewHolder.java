package com.example.proj;

import android.view.View;
import android.widget.TextView;

public class ViewHolder {
    TextView textView;
    /**
     * this class is for setting the text contents inside the tree nodes
     * **/
    ViewHolder(View view) {
        textView = view.findViewById(R.id.idTvNode);

    }
}
