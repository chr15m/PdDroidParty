package cx.mccormick.pddroidparty;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Math;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.os.SystemClock;
import android.util.Log;

import org.puredata.core.PdBase;

public class DroidNetReceive extends Widget {
	private static final String TAG = "DroidNetReceive";
	int port;
	Thread ServerThread;
	Socket connection = null;	
	
	public DroidNetReceive(PdDroidPatchView app, String[] atomline) {
		super(app);

		port = (int)Float.parseFloat(atomline[5]);
		sendname = app.app.replaceDollarZero(atomline[6]) + "-snd";
		receivename = app.app.replaceDollarZero(atomline[6]) + "-rcv";
		setupreceive();
		
		ServerThread = new Thread(ServerRun);
		ServerThread.start();
	}

	private Runnable ServerRun = new Runnable()
	{
		@Override
	    public void run()
        {
			while(true) do_connection();
        }
		
		void do_connection()
		{
			ServerSocket server= null;
			char[] buffer = new char[1024];
	        int bufLen = 0;
	        int Byte;
			
			try{
				server = new ServerSocket(port);
				Log.d(TAG, "server running on port "+port );
				connection = server.accept(); // wait for connection
				Log.d(TAG,"Connection from " + connection.getInetAddress().getHostName());

				do{
					Byte = connection.getInputStream().read();
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
				} while((Byte > 0)/*&&connection.isConnected()*/);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
			finally{
				try{
					Log.d(TAG,"connection closed");
					connection.close();
					connection = null;
					server.close();
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
			}
		}
	};  	
	
	
	public void receiveList(Object... args) {
	}
	
	public void receiveSymbol(String symbol) {
	}
	
	public void receiveFloat(float x) {
	}
	
	public void receiveBang() {
	}

	
	public void receiveMessage(String symbol, Object... args) {
	}

}
