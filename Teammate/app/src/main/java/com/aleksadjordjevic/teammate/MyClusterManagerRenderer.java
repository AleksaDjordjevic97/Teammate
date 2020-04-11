package com.aleksadjordjevic.teammate;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker>
{
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int margkerWidth;
    private final int margkerHeight;
    private Context cntx;

    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager)
    {
        super(context, map, clusterManager);

        cntx = context;
        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        margkerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        margkerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(margkerWidth,margkerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding,padding,padding,padding);
        iconGenerator.setContentView(imageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions)
    {
       // imageView.setImageResource(item.getIconPicture());
        Glide.with(cntx)
                .load(item.getIconPicture())
                 .placeholder(R.drawable.user)
                .into(imageView);

       // imageView.setImageURI(item.getIconPicture());
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster)
    {
        return false;
    }
}
