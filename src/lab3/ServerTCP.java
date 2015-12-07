package lab3;

import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.plaf.SliderUI;
 
public class ServerTCP extends Thread {
	public static void main(String args[]) {
		
			
			try {
				
			} catch (NumberFormatException e) {
				System.err.println("Wprowadz poprawny numer portu: " + e);
				return;
			}
			try {
				int i=0;//numer serwera
				boolean utworzona=false;
				ServerSocket serverSocket = new ServerSocket(4333);
				Executor exe = Executors.newFixedThreadPool(3);//tylko 3 jednoczesnie
				while(true)
				{
				Socket clientSocket = serverSocket.accept();
				if(clientSocket.isConnected()==true)
				{
					i++;
					
					ServerTCPThread gniazdo= new ServerTCPThread(clientSocket ,i,utworzona);
					exe.execute(gniazdo);
					utworzona=true;
				}
				}
		
			} catch (Exception e) {
				System.err.println(e);
			}
			
		}
	}
