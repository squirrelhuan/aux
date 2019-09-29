package com.example.aux;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.helper.PermissionManager;
import cn.demomaster.huan.quickdeveloplibrary.helper.download.DownloadHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.download.DownloadTask;
import cn.demomaster.huan.quickdeveloplibrary.helper.download.OnDownloadProgressListener;
import cn.demomaster.huan.quickdeveloplibrary.helper.install.InstallHelper;
import cn.demomaster.huan.quickdeveloplibrary.util.FileUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.QDAndroidDeviceUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.QDDeviceHelper;
import cn.demomaster.huan.quickdeveloplibrary.util.QDLogger;
import cn.demomaster.huan.quickdeveloplibrary.util.ScreenShotUitl;

import static cn.demomaster.huan.quickdeveloplibrary.helper.PermissionManager.startInstallPermissionSettingActivity;
import static cn.demomaster.huan.quickdeveloplibrary.helper.download.DownloadHelper.PERMISSIONS_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {


    @BindView(R.id.btn_root)
    Button btn_root;
    @BindView(R.id.btn_download)
    Button btn_download;
    @BindView(R.id.btn_update_file)
    Button btn_update_file;
    @BindView(R.id.btn_get_machineId)
    Button btn_get_machineId;
    @BindView(R.id.btn_isSimulator)
    Button btn_isSimulator;
    @BindView(R.id.btn_screenshot)
    Button btn_screenshot;
    @BindView(R.id.btn_pixel)
    Button btn_pixel;
    @BindView(R.id.btn_download_install)
    Button btn_download_install;
    


    @BindView(R.id.iv_shoot)
    ImageView iv_shoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btn_root.setOnClickListener(this);
        btn_download.setOnClickListener(this);
        btn_update_file.setOnClickListener(this);
        btn_get_machineId.setOnClickListener(this);
        btn_isSimulator.setOnClickListener(this);
        btn_screenshot.setOnClickListener(this);
        btn_pixel.setOnClickListener(this);
        btn_download_install.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_root:
               Toast.makeText(this, QDAndroidDeviceUtil.isRoot()?"root":"no root",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_download:
                download();
                break;
            case R.id.btn_update_file:
                editFile();
                break;
            case R.id.btn_get_machineId:
                Toast.makeText(this, getAndroidID(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_isSimulator:
                Toast.makeText(this, QDAndroidDeviceUtil.isSimulator(this)?"模拟器":"真机",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_screenshot:
                iv_shoot.setImageBitmap(ScreenShotUitl.shotActivity(this,45,125,130,158));
                Toast.makeText(this, "已截图",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_pixel:
                int color = ScreenShotUitl.pixel(this,1,2);
                int red = (color & 0xff0000) >> 16;
                int green = (color & 0x00ff00) >> 8;
                int blue = (color & 0x0000ff);
                QDLogger.d("color = "+color+",red="+red+",green="+green+",blue="+blue);
                break;
            case R.id.btn_download_install:
                updateApp(this,"https://b6.market.xiaomi.com/download/AppStore/084df452cadba44cb1b73603138d7fbe8aef2b76d/com.kuaiduizuoye.scan.apk");
                break;
        }
    }

    //app更新
    private void updateApp(final Activity context,String url) {

            //兼容8.0 安装权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    // Toast.makeText(context, "请先开启应用安装权限", Toast.LENGTH_SHORT).show();
                    //弹框提示用户手动打开
                    showAlert(context, "安装权限", "需要打开允许来自此来源，请去设置中开启此权限", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //此方法需要API>=26才能使用
                            startInstallPermissionSettingActivity(context);
                        }
                    });
                    return;
                }
            }

            //存储权限
            PermissionManager.chekPermission(context, PERMISSIONS_STORAGE, new PermissionManager.OnCheckPermissionListener() {
                @Override
                public void onPassed() {
                    InstallHelper.downloadAndInstall(context,"tmp.apk", url);
                }

                @Override
                public void onNoPassed() {

                }
            });
    }

    /**
     * alert 消息提示框显示
     * @param context  上下文
     * @param title    标题
     * @param message  消息
     * @param listener 监听器
     */
    public void showAlert(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", listener);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 获取设备id
     * @return
     */
    public static String getAndroidID() {
        String id = Settings.Secure.getString(
                MyApp.getInstance().getContentResolver(),Settings.Secure.ANDROID_ID);
        return id == null ? "" : id;
    }

    /**
     * 文本编辑
     */
    private void editFile() {
        /*创建文本 */
        File file = new File(Environment.getExternalStorageDirectory()+"/aux/abc.txt");
        if(!file.exists()){
            try {
                //file.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*写入文本*/
        FileUtil.writeFileSdcardFile(file.getAbsolutePath(),"hello \n\r 你好",false);
        Toast.makeText(this, "文件已经修改",Toast.LENGTH_SHORT).show();
    }

    /**
     * 文件下载
     */
    private void download() {
        DownloadHelper.DownloadBuilder downloadBuilder = new DownloadHelper.DownloadBuilder(this)
                .setFileName("abc.apk")
                .setUrl("http://shouji.360tpcdn.com/171112/95a45aef6293a954d7c6bc84e4519985/com.grand.arcade_14.apk")
                .setOnProgressListener(new OnDownloadProgressListener() {
                    @Override
                    public void onComplete(DownloadTask downloadTask) {
                        QDLogger.i(downloadTask.getFileName() + "下载完成" + downloadTask.getDownIdUri().getPath());
                    }

                    @Override
                    public void onProgress(long downloadId, String name, float fraction) {
                        QDLogger.i("下载进度" + fraction);
                    }
                });
        downloadBuilder.start();
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        QDLogger.i("触摸x="+motionEvent.getX()+",y="+motionEvent.getY());
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        QDLogger.i("触摸分发x="+ev.getX()+",y="+ev.getY());
        return super.dispatchTouchEvent(ev);
    }
}
