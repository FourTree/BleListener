package cn.paytube.testblelistener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.paytube.testblelistener.R;

@SuppressLint({ "DefaultLocale", "SimpleDateFormat" }) @SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    public static final int TIME_TO_STOP_SCANNING = 10000;
    public static final int BLE_SCAN_DEVICE_LOG_INDEX = 0;
    public static final String configFileName = "BleListenerConfig.properties";
    
   // @SuppressLint("UseValueOf") long bleaddrstart = new Long("1546591347457");//0x016818072301，21683010024D
    long bleaddrstart = Long.valueOf("BAC900885390", 16);
    long bleAddrStartsSave= Long.valueOf("BAC900885390", 16);

    int bleaAddrStep = 16;
    boolean flagbleaDeviceSaved = false;
    
    boolean flagDebug = false;
    static final String cfgFileDir = Environment.getExternalStorageDirectory()+"/Android/data/cn.paytube.testblelistener";

	boolean  flagScanAll = false;
	TextView edt_bledevice_sum;
	TextView edt_bledevice_list;

	int bleDeviceSumSave = 0;
    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private ArrayList<LEDEV> scanLeDev = new ArrayList<LEDEV>();

    boolean isScanning = false;
    boolean isScanningAll = false;
    int cntScan = 0;
    private BluetoothAdapter mBluetoothAdapter;
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       //ListView listView = (ListView) findViewById(R.id.list);
       //listView.setAdapter(mAdapter = new DeviceListAdapter(this, 0));
       //mAdapter = new DeviceListAdapter(this, 0);

       BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
       mBluetoothAdapter = mBluetoothManager.getAdapter();
      
       edt_bledevice_sum =  (TextView) this.findViewById(R.id.editText_blescan_sum);
       edt_bledevice_list = (TextView) this.findViewById(R.id.editText_bledev_name_addr);

       try {
    	   File cfgfiledir = new File(cfgFileDir);
    	   if(!cfgfiledir.exists()){
    		   cfgfiledir.mkdir();  
    	   }
    	   File cfgfile = new File(cfgFileDir+File.separator + configFileName);

    	   if(cfgfile.exists()){
        	   BufferedReader cfgreader = new BufferedReader(new FileReader(cfgfile));
    		   String cfgstr = cfgreader.readLine();
    		   cfgstr.replace(" ", "");
	    	   System.out.println(cfgstr);
	    	   String[] t = cfgstr.split(",");
	    	   if(t != null){
		    	   if(t.length == 3){
		    		   flagDebug = t[0].equals("0")? false:true;
		    		   bleaAddrStep = Integer.valueOf(t[1]);
		    		   bleaddrstart =  Long.valueOf(t[2], 16);
		    	   }else if(t.length == 2){
		    		   bleaAddrStep = Integer.valueOf(t[0]);
		    		   bleaddrstart =  Long.valueOf(t[1], 16);
		    	   }else if(t.length == 1){
		        	   bleaddrstart = Long.valueOf(t[0], 16);;
		    	   }
	    	   }
	    	   cfgreader.close();
    	   }else{
    		   cfgfile.createNewFile();
        	   BufferedWriter cfgwriter = new BufferedWriter(new FileWriter(cfgfile));
        	   String cfgstr = (flagDebug?"1,":"0,") + Integer.toString(bleaAddrStep)+","+Long.toHexString(bleaddrstart);
        	   cfgwriter.write(cfgstr);
        	   cfgwriter.close();
    	   }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
       
       LogUtil.setLogFile(0, "BleDeviceScan_");
       if(!flagDebug){
			//buttonDisablle((Button)findViewById(R.id.scan_all));
			//buttonDisablle((Button)findViewById(R.id.next_pan));
			//buttonDisablle((Button)findViewById(R.id.up_pan));
			//buttonDisablle((Button)findViewById(R.id.next_addr));
       }
	
       edt_bledevice_sum.setText("起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));

       findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isScanning){
					edt_bledevice_sum.setText("#搜索停止#\r\n"+"起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
					scanEnd();
				}else{					
					((Button)findViewById(R.id.scan)).setTextColor(Color.RED);
					((Button)findViewById(R.id.scan)).setText("Scanning");
					
					buttonDisablle((Button)findViewById(R.id.scan_all));
					buttonDisablle((Button)findViewById(R.id.save));
					buttonDisablle((Button)findViewById(R.id.setting));
					buttonDisablle((Button)findViewById(R.id.up_pan));
					buttonDisablle((Button)findViewById(R.id.next_addr));
					buttonDisablle((Button)findViewById(R.id.next_pan));
	
					scanBLEDevices();
				}
			}
		});
       findViewById(R.id.scan_all).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isScanningAll){
					isScanningAll = false;
					flagScanAll = false;
					edt_bledevice_sum.setText("#搜索停止#\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
					scanAllEnd();
					return ;
				}
				
				flagScanAll = true;
				isScanningAll = true;
				((Button)findViewById(R.id.scan_all)).setTextColor(Color.YELLOW);
				

				buttonDisablle((Button)findViewById(R.id.save));
				buttonDisablle((Button)findViewById(R.id.scan));

				scanAllBLEDevices();
			}
		});
       findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!flagScanAll){
					if(!devices.isEmpty()){
						flagbleaDeviceSaved = false;
						if((bleAddrStartsSave != bleaddrstart) || (devices.size() > bleDeviceSumSave)){
							bleAddrStartsSave = bleaddrstart;
		    				bleDeviceSumSave = devices.size();
		    				save_scanning_log(bleaddrstart,bleDeviceSumSave,devices);
		    				saveConfig();
						}
	    				edt_bledevice_sum.setText("#已保存日志#\r\n起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
					}else{
	    				edt_bledevice_sum.setText("#已保存日志#\r\n起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
    				}
        		}
			}
		});
       
       findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			       final EditText inputSetting = new EditText(MainActivity.this);
			       AlertDialog.Builder builderSetting = new AlertDialog.Builder(MainActivity.this);
			       builderSetting.setTitle("Setting").setIcon(android.R.drawable.ic_dialog_info).setView(inputSetting)
			               .setNegativeButton("Cancel", null);
			       inputSetting.setText(Integer.toString(bleaAddrStep)+","+Long.toHexString(bleaddrstart).toUpperCase());
			       builderSetting.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			       public void onClick(DialogInterface dialog, int which) {
			    	   String instr= inputSetting.getText().toString();
			    	   instr.replace(" ", "");
			    	   System.out.println(instr);
			    	   String[] t = instr.split(",");

			    	   if(t.length == 3){
			    		   flagDebug = t[0].equals("0")? false:true;
			    		   bleaAddrStep = Integer.valueOf(t[1]);
			    		   bleaddrstart =  Long.valueOf(t[2], 16);
			    	   }else if(t.length == 2){
			    		   bleaAddrStep = Integer.valueOf(t[0]);
			    		   bleaddrstart =  Long.valueOf(t[1], 16);
			    	   }else if(t.length == 1){
			        	   bleaddrstart = Long.valueOf(t[0], 16);;
			    	   }
			    	   saveConfig();
		        	   
				       edt_bledevice_sum.setText("起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase());
				       edt_bledevice_list.setText("搜索到的蓝牙设备名称和地址:");            }
			       });
				builderSetting.show();
			}
		});
       
       findViewById(R.id.next_pan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flagbleaDeviceSaved = false;
				bleaddrstart+=bleaAddrStep;
				bleAddrStartsSave = 0;
				saveConfig();
				devices.clear();
				scanLeDev.clear();
		        edt_bledevice_sum.setText("起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase());
		        edt_bledevice_list.setText("搜索到的蓝牙设备名称和地址:");
			}
		});
       findViewById(R.id.up_pan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flagbleaDeviceSaved = false;
				bleaddrstart-=bleaAddrStep;
				bleAddrStartsSave = 0;
		    	saveConfig();

				devices.clear();
				scanLeDev.clear();
		        edt_bledevice_sum.setText("起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase());
		        edt_bledevice_list.setText("搜索到的蓝牙设备名称和地址:");
			}
		});
       
       
       findViewById(R.id.next_addr).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flagbleaDeviceSaved = false;
				bleaddrstart+=1;
				bleAddrStartsSave = 0;
		    	saveConfig();
				devices.clear();
				scanLeDev.clear();
		        edt_bledevice_sum.setText("起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase());
		        edt_bledevice_list.setText("搜索到的蓝牙设备名称和地址:");
			}
		});
    }
    @SuppressLint("NewApi")
	private void scanAllBLEDevices() {
    	System.out.println("####scanBLEDevices#####");

       // mAdapter.clear();
        mBluetoothAdapter.stopLeScan(leScanAllCallback);
        mBluetoothAdapter.startLeScan(leScanAllCallback);
       	devices.clear();
       	
        edt_bledevice_sum.setText("起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase());
        edt_bledevice_list.setText("搜索到的蓝牙设备名称和地址:");
    }
    @SuppressLint("NewApi")
	private void scanBLEDevices() {
    	System.out.println("####scanBLEDevices#####");
    	isScanning = true;
    	
       // mAdapter.clear();
        mBluetoothAdapter.stopLeScan(leScanCallback);
        mBluetoothAdapter.startLeScan(leScanCallback);
        if((devices.size() == bleaAddrStep) || (devices.size() == 0)){
			devices.clear();
			scanLeDev.clear();
	       	edt_bledevice_list.setText("搜索到的蓝牙设备名称和地址:");
        }
         edt_bledevice_sum.setText("#正在搜索...#\r\n起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(scanLeDev.size()));


//        cntScan = (TIME_TO_STOP_SCANNING/1000 -1);
//        btnCounter();
//        new Handler().postDelayed(new Runnable() {
//            @SuppressLint("NewApi")
//			@Override
//            public void run() {
//            	scanEnd();
//            }
//        }, TIME_TO_STOP_SCANNING);
    }
    void btnCounter(){
    	new Handler().postDelayed(new Runnable() {
            @SuppressLint("NewApi")
			@Override
            public void run() {
            	if(cntScan > 0){
                	((Button)findViewById(R.id.scan)).setText("Scan("+(cntScan --)+")");
            		btnCounter();
            	}else{
                	((Button)findViewById(R.id.scan)).setText("Scan");
            	}
            }
        }, 1000);
    }
    @SuppressLint("NewApi") void scanAllEnd(){
        mBluetoothAdapter.stopLeScan(leScanAllCallback);
        devices.clear();
		((Button)findViewById(R.id.scan)).setTextColor(Color.BLACK);
		((Button)findViewById(R.id.scan)).setText("Scan");
		
       // if(flagDebug){
        	buttonEnablle((Button)findViewById(R.id.scan_all));
        //}
        buttonEnablle((Button)findViewById(R.id.save));
        buttonEnablle((Button)findViewById(R.id.scan));
        
        buttonEnablle((Button)findViewById(R.id.setting));
        buttonEnablle((Button)findViewById(R.id.up_pan));
        buttonEnablle((Button)findViewById(R.id.next_pan));
        buttonEnablle((Button)findViewById(R.id.next_addr));
    }
    
    @SuppressLint("NewApi") void scanEnd(){
    	isScanning = false;
        mBluetoothAdapter.stopLeScan(leScanCallback);
		((Button)findViewById(R.id.scan)).setTextColor(Color.BLACK);
		((Button)findViewById(R.id.scan)).setText("Scan");
		
       // if(flagDebug){
        	buttonEnablle((Button)findViewById(R.id.scan_all));
        //}
        buttonEnablle((Button)findViewById(R.id.save));
        buttonEnablle((Button)findViewById(R.id.scan));
        
        buttonEnablle((Button)findViewById(R.id.setting));
        buttonEnablle((Button)findViewById(R.id.up_pan));
        buttonEnablle((Button)findViewById(R.id.next_pan));
        buttonEnablle((Button)findViewById(R.id.next_addr));
    }
    @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!devices.contains(device)) {
	            	long tadd = bleaddr2long(device.getAddress());
	            	if((bleaddrstart <= tadd)&& (tadd < (bleaddrstart+bleaAddrStep))){
	            		devices.add(device);
	            		
	            		LEDEV leDev = new LEDEV(device,rssi);
	            		scanLeDev.add(leDev);
		            	
	 	                edt_bledevice_sum.setText("#正在搜索...#\r\n起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(scanLeDev.size()));
	 	                String tmp = "搜索到的蓝牙设备名称和地址:";
	 	               
	 	                for(int i=0;i<bleaAddrStep;i++){
		 	          		for(int index=0;index < scanLeDev.size();index++){
		 	          			BluetoothDevice btdev = scanLeDev.get(index).getLedev();
		 	      	    		if(bleaddr2long(btdev.getAddress()) == (bleaddrstart+i)){
		 	      	    			tmp +=("\r\n"+btdev.getName()+"    0x"+Long.toHexString(bleaddrstart+i).toUpperCase())+"   RSSI="+scanLeDev.get(index).getRssi();
		 	      	    	    	break;
		 	      	    		}
		 	      	    		if(index == (scanLeDev.size()-1)){
		 	      	    			;
		 	      	    		}
		 	          		}
	 	                }	
	 	                edt_bledevice_list.setText(tmp);
	            	}
	            	if(devices.size() == bleaAddrStep){
	            		if(!flagbleaDeviceSaved){
	            			if((bleAddrStartsSave == bleaddrstart) && (devices.size() == bleDeviceSumSave)){
	            				edt_bledevice_sum.setText("#搜索完成,已保存日志#\r\n"+"起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
	            			}else{
	            				flagbleaDeviceSaved = false;
	            				bleAddrStartsSave = bleaddrstart;
	            				bleDeviceSumSave = devices.size();
	            				save_scanning_log(bleaddrstart,bleDeviceSumSave,devices);
	            				saveConfig();
	            				edt_bledevice_sum.setText("#搜索完成,已保存日志#\r\n"+"起始地址：0x"+Long.toHexString(bleaddrstart).toUpperCase()+"\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
	            			}
	            		}
            			scanEnd();
	            	}
        }
      }
    };
    
    @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback leScanAllCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!devices.contains(device)) {
            		devices.add(device);
            		edt_bledevice_sum.setText("#正在搜索...#\r\n搜索到的蓝牙设备总数："+bleaddr2str(devices.size()));
            		String tmp = edt_bledevice_list.getText().toString();
	 	            edt_bledevice_list.setText(tmp+"\r\n"+device.getName()+"    "+device.getAddress()+"   RSSI="+rssi);
            }
      }
    };
    @Override
	public void onBackPressed() {
    	try{
    		
    		  android.os.Process.killProcess(android.os.Process.myPid());    //获取PID 
    		  System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
    
    String bleaddr2str(int iaddr){
    	String str = Integer.toString(iaddr);
    	return str;
    }
    
    @SuppressLint("UseValueOf") long bleaddr2long(String saddr){
    	String addr = saddr.replace(":", "");
    	
    	return Long.valueOf(addr, 16);
    }
    
    void save_scanning_log(long devaddrstart,int n,ArrayList<BluetoothDevice> dev ){
    	
    	LogUtil.print(BLE_SCAN_DEVICE_LOG_INDEX, n+"，"+"0x"+Long.toHexString(devaddrstart).toUpperCase()+","+datetime());
//    	for(int i=0;i<n;i++){
//    		for(int index=0;index < n;index++){
//    			BluetoothDevice btdev = dev.get(index);
//	    		if(bleaddr2long(btdev.getAddress()) == (devaddrstart+i)){
//	    	    	LogUtil.print(BLE_SCAN_DEVICE_LOG_INDEX, btdev.getName()+", 0x"+Long.toHexString(devaddrstart).toUpperCase());
//	    	    	break;
//	    		}
//	    		if(index == (n-1)){
//	    	    	LogUtil.print(BLE_SCAN_DEVICE_LOG_INDEX, "################, 0x############");
//	    		}
//    		}
//    	}
    	
    	for(int i=0;i<bleaAddrStep;i++){
       		for(int index=0;index < scanLeDev.size();index++){
       			BluetoothDevice btdev = scanLeDev.get(index).getLedev();
   	    		if(bleaddr2long(btdev.getAddress()) == (bleaddrstart+i)){
	    	    	LogUtil.print(BLE_SCAN_DEVICE_LOG_INDEX, "  "+btdev.getName()+", 0x"+Long.toHexString(bleaddrstart+i).toUpperCase()+",RSSI="+scanLeDev.get(index).getRssi());
   	    	    	break;
   	    		}
   	    		if(index == (scanLeDev.size()-1)){
   	    			LogUtil.print(BLE_SCAN_DEVICE_LOG_INDEX, "  ################, 0x############");
   	    		}
       		}
         }	
    }
    
    private static String datetime()  
    {  
    	String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System  
                .currentTimeMillis()));  
    	
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss SSS",
				Locale.CHINA);
		String time = format.format(new Date(System.currentTimeMillis()));
		return date+time;
    }
    
    void buttonEnablle(Button bt){
    	bt.setTextColor(Color.BLACK);
    	bt.setEnabled(true);
    }
    
    void buttonDisablle(Button bt){
    	bt.setTextColor(Color.GRAY);
    	bt.setEnabled(false);
    }
    
    void saveConfig(){
    	try {
       	   File cfgfile = new File(cfgFileDir+File.separator + configFileName);
 		   BufferedWriter cfgwriter;
 		   cfgwriter = new BufferedWriter(new FileWriter(cfgfile));
 		   String cfgstr = (flagDebug?"1,":"0,") + Integer.toString(bleaAddrStep)+","+Long.toHexString(bleaddrstart);
     	   cfgwriter.write(cfgstr);
     	   cfgwriter.close();
     	 } catch (IOException e) {
     		   // TODO Auto-generated catch block
     		   e.printStackTrace();
     	 }
    }
    
    String getConfig(){
    	return null;
    }
}

class LEDEV{
	BluetoothDevice leDev;
	int Rssi;
	
	
	LEDEV(BluetoothDevice ledev,int rssi){
		leDev = ledev;
		Rssi = rssi;
	}
	
	public BluetoothDevice getLedev() {
		return leDev;
	}
	public void setLedev(BluetoothDevice ledev) {
		this.leDev = ledev;
	}
	public int getRssi() {
		return Rssi;
	}
	public void setRssi(int rssi) {
		this.Rssi = rssi;
	}
	
}


