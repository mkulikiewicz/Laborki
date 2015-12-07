package lab3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

import javax.xml.bind.ParseConversionEvent;
 
public class ClientTCP extends Thread  {
	public static void main(String args[]){
	
		
			try {
				int linijka=0;//odczyt i wyswietlanie lini
				String odpowiedza;//zmienna do odpowiedzi
				boolean koniec=true;//pozwala konczyc while :D
				//----utworzenie socketu i baforu
				Socket socket = new Socket(InetAddress.getByName("localhost"), 4333);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//-----odczyt numeru studenta
				System.out.print("Numer indeksu\n");
				Scanner nr = new Scanner(System.in);
				String numer_indeksu=nr.nextLine();
				out.println(numer_indeksu);
				//------odczyt pytan
				System.out.print("\nPytania\n\n");
				while(koniec)
				{
					if(in.ready()==true)//jak wysyla
					{		
						String sprawdz_czy_koniec=in.readLine();//przypisz
						System.out.println(sprawdz_czy_koniec);//wyswietl
						linijka++;
						if(linijka == 6)//po szeciu liniach
						{
							System.out.println("odpowiedz to :" );
							Scanner sc = new Scanner(System.in);
							odpowiedza=sc.nextLine();
							out.println(odpowiedza);//przekaz odp
							linijka=0;//wyzeruj
						}
						sleep(100);
						if(sprawdz_czy_koniec.startsWith("Twoj", 0))//zakoncz whila jak bedziesz znac wynik	
						koniec=false;

					}		
				}
				socket.close();
				System.out.println("Dziêkujemy za poœwiecony czas" );		
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
