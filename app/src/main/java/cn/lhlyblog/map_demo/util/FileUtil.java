package cn.lhlyblog.map_demo.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    /*private Context context;

    public FileUtil(Context context) {
        this.context = context;
    }

    public void fileOperation() {
        try {
            // 先获取系统默认的文档存放根目录
            File parent_path = Environment.getExternalStorageDirectory();
            File dir = new File(parent_path.getAbsoluteFile(), "data");
            if(!dir.exists()){
                dir.mkdir();
            }
            File file = new File(dir.getAbsoluteFile(), "style.data");
            if(file.exists()){
                return;
            }
            //读取数据文件
            InputStream open = context.getResources().getAssets().open("styleMap/style.data");

            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            int len;
            byte[] buf = new byte[1024];
            while((len=open.read(buf))!=-1){
                fos.write(buf,0,len);
            }
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
