package app.led;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Jin-Seok on 2016-12-21.
 */
public class GridAdapter extends BaseAdapter
{

    private final static int size = 85;
    private Context mContext;

    public GridAdapter(Context c)
    {
        this.mContext = c;
    }

    private Integer[] mThumbIds =
            {
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,
                    R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell,R.drawable.cell
            };

    @Override
    public int getCount()
    {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView;

        if(convertView == null)
        {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        }
        else
        {
            imageView = (ImageView)convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

}
