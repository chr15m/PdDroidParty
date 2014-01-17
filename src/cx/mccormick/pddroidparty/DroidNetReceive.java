package cx.mccormick.pddroidparty;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class DroidNetReceive extends Widget {
	private static final String TAG = "DroidNetReceive";
	int port;
	String connection_type = null;
	Thread ServerThread;
	Socket connection = null;	
	
	public DroidNetReceive(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		port = (int)Float.parseFloat(atomline[5]);
		sendname = app.app.replaceDollarZero(atomline[6]);
		receivename = app.app.replaceDollarZero(atomline[6]) + "-rcv";
		if (atomline.length > 7) {
			connection_type = app.app.replaceDollarZero(atomline[7]);
			
		}
		Log.e(TAG, "Connection type: " + connection_type);
		
		ServerThread = new Thread(ServerRun);
		ServerThread.start();
	}
	
	private Runnable ServerRun = new Runnable() {
		@Override
		public void run() {
			while(true) {
				if (connection_type != null) {
					do_udp_connection();
				} else {
					do_tcp_connection();
				}
			}
		}
		
		void do_tcp_connection() {
			ServerSocket server= null;
			char[] buffer = new char[1024];
			int bufLen = 0;
			int Byte;
			
			try {
				server = new ServerSocket(port);
				Log.d(TAG, "server running on port "+port );
				connection = server.accept(); // wait for connection
				Log.d(TAG,"Connection from " + connection.getInetAddress().getHostName());

				do {
					Byte = connection.getInputStream().read();
					if(Byte > 0) {
						if (Byte == '\n') { /*do nothing, real end of line on pd is ';'. bufLen = 0;*/ }
						else if (Byte == ';') { // pd end of line
							if(bufLen != 0) {
								String text = new String(buffer, 0, bufLen);
								if (text.endsWith("\n")) {
									text = text.substring(0, text.length() - 2);
								}
								if (text.endsWith("\r")) {
									text = text.substring(0, text.length() - 2);
								}
								if (text.endsWith(";")) {
									text = text.substring(0, text.length() - 2);
								}
								send(text);
							}
							bufLen = 0;
						} else {
							buffer[bufLen++] = (char)Byte;
							if(bufLen >= 1024) bufLen = 0;
						}
					}
				} while((Byte > 0)/*&&connection.isConnected()*/);
			}
			catch(IOException ioException) {
				ioException.printStackTrace();
			}
			finally {
				try {
					Log.d(TAG,"connection closed");
					connection.close();
					connection = null;
					server.close();
				}
				catch(IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
		
		void do_udp_connection() {
			try {
				Log.d(TAG, "Server running on port " + port);
				DatagramSocket s = new DatagramSocket(port);
				while (true) {
					byte[] message = new byte[1024];
					DatagramPacket p = new DatagramPacket(message, message.length);
					s.receive(p);
					String text = new String(message, 0, p.getLength());
					if (text.endsWith("\n")) {
						text = text.substring(0, text.length() - 2);
					}
					if (text.endsWith("\r")) {
						text = text.substring(0, text.length() - 2);
					}
					if (text.endsWith(";")) {
						text = text.substring(0, text.length() - 2);
					}
					send(text);
				}
			} catch(IOException e) {
				e.printStackTrace();
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
