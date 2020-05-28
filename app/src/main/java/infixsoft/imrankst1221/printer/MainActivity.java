package infixsoft.imrankst1221.printer;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.android.gms.ads.AdView;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity{

    EditText message;
    Button btnPrint;
    LinearLayout layout_content;
    private static BluetoothSocket btsocket;
    private static OutputStream outputStream;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPrint = (Button)findViewById(R.id.btnPrint);
        layout_content = findViewById(R.id.layout_content);
        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printDemo();
            }
        });
    }

    protected void printDemo() {
        if(btsocket == null){
            Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
            this.startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
        }
        else{
            OutputStream opstream = null;
            try {
                opstream = btsocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = opstream;

            //print command
            try {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                outputStream = btsocket.getOutputStream();
                printPhoto();
                printNewLine();
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //print photo
    public void printPhoto() {
        layout_content.setDrawingCacheEnabled(true);
        layout_content.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layout_content.layout(0, 0, layout_content.getMeasuredWidth(), layout_content.getMeasuredHeight());
        layout_content.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(layout_content.getDrawingCache());
        layout_content.setDrawingCacheEnabled(false);
        try {
            Bitmap bmp = toGrayscale(b);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(btsocket!= null){
                outputStream.close();
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = DeviceList.getSocket();
            if(btsocket != null){
                printPhoto();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap toGrayscale(Bitmap srcImage) {

        Bitmap bmpGrayscale = Bitmap.createBitmap(srcImage.getWidth(),
                srcImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(srcImage, 0, 0, paint);
        return bmpGrayscale;
    }
}