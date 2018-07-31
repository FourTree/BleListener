package cn.paytube.testblelistener;


import java.io.File;  
import java.io.FileWriter;  
import java.io.IOException;  
import java.io.PrintWriter;  
import java.io.StringWriter;  
import java.net.UnknownHostException;  
import java.text.SimpleDateFormat;  
import java.util.Date;  
  
import java.util.Locale;



import android.annotation.SuppressLint;  
import android.os.Environment;  
import android.util.Log;  
  
/** 
 * Android开发调试日志工具类[支持保存到SD卡]<br> 
 * <br> 
 *  
 * 需要一些权限: <br> 
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> <br> 
 * <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" /><br> 
 *  
 * @author PMTOAM 
 *  
 */  
@SuppressLint("SimpleDateFormat")  
public class LogUtil  
{  
  
    public static final String CACHE_DIR_NAME = "mpsbletoolsLog";  
    public static String[] Log_file_name = new String[4];  

      
    public static boolean isDebugModel = true;// 是否输出日志  
    public static boolean isSaveDebugInfo = true;// 是否保存调试日志  
    public static boolean isSaveCrashInfo = true;// 是否保存报错日志  
    public static boolean logflag = false;// 是否保存报错日志  
    public static void logflag(boolean is)  
    {  
       logflag=is;
    } 
    
    public static void v(final String tag, final String msg)  
    {  
        if (isDebugModel)  
        {  
            Log.v(tag, "--> " + msg);  
        }  
    }  
  
    public static void d(final String tag, final String msg)  
    {  
        if (isDebugModel)  
        {  
            Log.d(tag, "--> " + msg);  
        }  
    }  
  
    public static void i(final String tag, final String msg)  
    {  
        if (isDebugModel)  
        {  
            Log.i(tag, "--> " + msg);  
        }  
    }  
  
    public static void w(final String tag, final String msg)  
    {  
        if (isDebugModel)  
        {  
            Log.w(tag, "--> " + msg);  
        }  
    }  
  
    /** 
     * 调试日志，便于开发跟踪。 
     * @param tag 
     * @param msg 
     */  
    public static void e(final String tag, final String msg)  
    {  
        if (isDebugModel)  
        {  
            Log.e(tag, "--> " + msg);  
        }  
  
        if (isSaveDebugInfo)  
        {  
            new Thread()  
            {  
                public void run()  
                {  
                	print(time() + tag + " --> " + msg + "\n");  
                };  
            }.start();  
        }  
    }  
  
    /** 
     * try catch 时使用，上线产品可上传反馈。 
     * @param tag 
     * @param tr 
     */  
    public static void e(final String tag, final Throwable tr)  
    {  
        if (isSaveCrashInfo)  
        {  
            new Thread()  
            {  
                public void run()  
                {  
                	print(time() + tag + " [CRASH] --> "  
                            + getStackTraceString(tr) + "\n");  
                };  
            }.start();  
        }  
    }  
  
    /** 
     * 获取捕捉到的异常的字符串 
     * @param tr 
     * @return 
     */  
    public static String getStackTraceString(Throwable tr)  
    {  
        if (tr == null)  
        {  
            return "";  
        }  
  
        Throwable t = tr;  
        while (t != null)  
        {  
            if (t instanceof UnknownHostException)  
            {  
                return "";  
            }  
            t = t.getCause();  
        }  
  
        StringWriter sw = new StringWriter();  
        PrintWriter pw = new PrintWriter(sw);  
        tr.printStackTrace(pw);  
        return sw.toString();  
    }  
  
    /** 
     * 标识每条日志产生的时间 
     * @return 
     */  
    private static String time()  
    {  
    	
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss SSS",
				Locale.CHINA);
		String time = format.format(new Date(System.currentTimeMillis()));
		return time;
//        return "["  
//                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss").format(new Date(  
//                        System.currentTimeMillis())) + "] ";  
    }  
  
    /** 
     * 以年月日作为日志文件名称 
     * @return 
     */  
    private static String date()  
    {  
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(System  
                .currentTimeMillis()));  
    }  
  
    /** 
     * 保存到日志文件 
     * @param content 
     */  
    public static synchronized void print(String content)  
    {  
    	System.out.println(content);
//        try  
//        {  
//            FileWriter writer = new FileWriter(getFile(), true);  
//            writer.write(time()+content+"\r\n");  
//            writer.close();  
//        }  
//        catch (IOException e)  
//        {  
//            e.printStackTrace();  
//        }  
    }  
  public static void setLogFile(int i ,String s){
	  if((i>4) || (i<0)) i=0;
	  Log_file_name[i] = s;
  }
  /** 
   * 保存到日志文件 i
   * @param content 
   */  
  public static synchronized void print(int i,String content)  
  {  
  	  System.out.println(content);
      try  
      {  
          FileWriter writer = new FileWriter(getFile(i), true);  
          writer.write(content+"\r\n");  
          writer.close();  
      }  
      catch (IOException e)  
      {  
          e.printStackTrace();  
      }  
  }  
  
    /** 
     * 获取日志文件路径 
     * @return 
     */  
    public static String getFile()  
    {  
        File sdDir = null;  
  
        if (Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED))  
            sdDir = Environment.getExternalStorageDirectory();  
  
        File cacheDir = new File(sdDir + File.separator + CACHE_DIR_NAME);  
        if (!cacheDir.exists()){  
            cacheDir.mkdir();  
        }else{
        	if(logflag){
        		  cacheDir.mkdir();  
        	}
        }
  
        File filePath = new File(cacheDir + File.separator + date() + ".log");  
  
        return filePath.toString();  
    }  
    
    /** 
     * 获取日志文件路径 
     * @return 
     */  
    public static String getFile(int i)  
    {  
        File sdDir = null;  
  
        if (Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED))  
            sdDir = Environment.getExternalStorageDirectory();  
  
        File cacheDir = new File(sdDir + File.separator + CACHE_DIR_NAME);  
        if (!cacheDir.exists()){  
            cacheDir.mkdir();  
        }else{
        	if(logflag){
        		  cacheDir.mkdir();  
        	}
        }
        if(i > 4)  i=0;
        File filePath = new File(cacheDir + File.separator + Log_file_name[i]+date() + ".log");  
  
        return filePath.toString();  
    }  
  
}  