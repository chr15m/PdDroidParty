package cx.mccormick.pddroidparty;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

public class DroidNetClient extends Widget {
	private static final String TAG = "DroidNetClient";
	int port = 0;
	InetAddress host;
	Thread ClientThread = null;
	Socket connection = null;	
	
	public DroidNetClient(PdDroidPatchView app, String[] atomline) {
		super(app);

		sendname = app.app.replaceDollarZero(atomline[5]) + "-snd";
		receivename = app.app.replaceDollarZero(atomline[5]) + "-rcv";
		setupreceive();		
	}

	private Runnable ClientRun = new Runnable()
	{
		@Override
	    public void run()
        {
			//Socket client= null;
			char[] buffer = new char[1024];
	        int bufLen = 0;
	        int Byte;
			
			try{
				connection = new Socket(host,port);
				Log.d(TAG, "connected to host " + host + " on port " + port );
				send("_connected_ 1");
				do{
					if(connection != null && connection.getInputStream() != null) 
						Byte = connection.getInputStream().read();
					else Byte = -1;
					
					if(Byte > 0) {
						if(Byte == '\n') { /*do nothing, real end of line on pd is ';'. bufLen = 0;*/ }
						else if(Byte == ';') { // pd end of line
							if(bufLen != 0) send(String.copyValueOf(buffer, 0, bufLen));
							bufLen = 0;
						} else {
							buffer[bufLen++] = (char)Byte;
							if(bufLen >= 1024) bufLen = 0;
						}
					}
				} while(Byte > 0);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
			finally{
				try{
					Log.d(TAG,"connection closed");
					if(connection != null) connection.close();
					connection = null;
					port = 0;
					send("_connected_ 0");
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
			}
		}
	};  	
		
	public void receiveMessage(String symbol, Object... args) {
		if(symbol.equals("connect")
		&& (args.length == 2) 
		&& args[0].getClass().equals(String.class)
		&& args[1].getClass().equals(Float.class)){
			if(port != 0) {
				Log.d(TAG,"already connected");
				return;
			}
			try {
				host = InetAddress.getByName(args[0].toString());
				port = (int)(float)(Float)args[1];
				ClientThread = new Thread(ClientRun);
				ClientThread.start();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(symbol.equals("disconnect")){
			if(port == 0) return;
			try {
				connection.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				port = 0;
			}
			
		}
		else if(symbol.equals("send")){
			if(connection == null) return;
			if( ! connection.isConnected() ) return;
			OutputStream out;
			try {
				out = connection.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			try {
				for(Object o : args) out.write(o.toString().getBytes());
				out.write(';');
				out.write('\n');
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
