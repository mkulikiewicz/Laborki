package lab3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 
public class ServerTCPThread extends Thread implements Runnable {
	Socket mySocket;
	int i;//numer watku
	boolean baza_utworzona;
	
	public ServerTCPThread(Socket socket , int i, boolean operacjanabazie)
	{
		super(); // konstruktor klasy Thread
		mySocket = socket;
		this.i= i;
		baza_utworzona=operacjanabazie;
	}
	
	public void utworz_baze(BufferedReader pytania, PrintWriter out) throws InterruptedException, IOException ,ExecutionException
	{	
		ExecutorService exe1 = Executors.newFixedThreadPool(1);
		String odpowiedzi_do_pytania="";
			exe1.submit(new Baza("KM"));
			exe1.submit(new Baza( "CREATE TABLE if not exists `km`.`baza_pytan` ( `id` INT NOT NULL , `pytanie` VARCHAR(255) NOT NULL , `odpowiedzi` VARCHAR(255) NOT NULL ) ENGINE = InnoDB;" , "dodaj" ));
			exe1.submit(new Baza( "CREATE TABLE if not exists `km`.`odpowiedzi_studenta` ( `numer_studenta` INT NOT NULL , `ocena` INT NOT NULL , `odpowiedzi` VARCHAR(255) NOT NULL ) ENGINE = InnoDB;" , "dodaj" ));
			exe1.submit(new Baza( "CREATE TABLE if not exists `km`.`baza_odpowiedzi` ( `id` INT NOT NULL , `odpowiedzi` TEXT NOT NULL ) ENGINE = InnoDB;" , "dodaj" ));
			exe1.submit(new Baza( "DELETE FROM `baza_pytan` WHERE 1" , "dodaj" ));
			
			for(int i=0;i<4;i++)
			{	
				exe1.submit( new Baza( "INSERT INTO `baza_pytan` (`id`, `pytanie`, `odpowiedzi`) VALUES ('"+i+"', '"+pytania.readLine()+"', '')" , "dodaj" ));  
				odpowiedzi_do_pytania="";
				for(int j=0;j<4; j++)
				{
					odpowiedzi_do_pytania= odpowiedzi_do_pytania+pytania.readLine()+"\n";
				}
				exe1.submit( new Baza( "UPDATE  baza_pytan set odpowiedzi='"+odpowiedzi_do_pytania+"' where id="+i+"" , "dodaj" ));
			}
	}
	
	public void utworz_odpowiedzi(int id,String odpowiedz) throws InterruptedException, IOException ,ExecutionException
	{	
		ExecutorService exe1 = Executors.newFixedThreadPool(1);
		String odpowiedzi_do_pytania="";
			exe1.submit(new Baza( "INSERT INTO `baza_odpowiedzi`(`id`, `odpowiedzi`) VALUES ("+id+","+odpowiedz+")" , "dodaj" ));	
	}
	
	public String zwroc_baze_pytañ(int id)
	{
		ExecutorService exe = Executors.newFixedThreadPool(1);
		Future <String> future =   exe.submit(new Baza( "SELECT * FROM `baza_pytan` WHERE id="+id+"" , "zwroc" ));
		return null;

	}
	public void run() // program watku
	{	
		try {
			String odpowiedzi_studenta="";
			
			int [] tab = new int [4];
			ExecutorService exe1 = Executors.newFixedThreadPool(1);
			int p=0;					//numer indeksu tablicy
			String numer_studenta;		//numer studenta
			PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);//strumien wyjscia servera
			BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));//strumien wejscia servera
			BufferedReader pytania = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("baza_pytan.txt"))));//odczyt pytan
			BufferedReader odpowiedzi = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("baza_odpowiedzi.txt"))));//odczyt odpowiedzi
			PrintWriter zapis = new PrintWriter(new BufferedWriter(new FileWriter("odpowiedzi_studentow.txt",true)));//zapis do pliku
			
			//----utworzenie nowej bazy i dodaj pytania
			if(baza_utworzona != true){
			utworz_baze(pytania,out);
			dodaj_poprawne_odpowiedzi(odpowiedzi);//wysylanie pytañ do bazy pytañ
			sleep(1000);//musi utworzyc cala zanim wysles
			}
			
			wyslij_z_bazy(out);
			//----pobranie numeru klienta
			numer_studenta=in.readLine();
			
			
			
			
			
			
			
		//	exe1.submit(new Baza( "INSERT INTO `baza_odpowiedzi`(`id`, `odpowiedzi`) VALUES ('"+numer_studenta+"','"+"Odpowiedzi kolejno :"+"')" , "dodaj" ));
			

			System.out.println("polaczony ze studentem numer: "+numer_studenta);
			//----------------------odczytywanie odpowiedzie
			while(p<5)//---dopuki nie ma 4 odpowiedzi
			{	
				if(in.ready()==true)//---jezeli podaje wejscie
				{	
					String zmienna_do_odpo=in.readLine();				
					if(zmienna_do_odpo.equals(odpowiedzi.readLine()))//----sprawdza czy odpowiedz wpisana jest 
					{													//		taka sama jak odpowiedz w pliku
						System.out.println("prawidlowa");
						tab[p]=1;
				//		odpowiedzi_studenta=odpowiedzi_studenta(numer_studenta,zmienna_do_odpo,odpowiedzi_studenta);
						p++;
					}
					else{												//-------jak nie
						System.out.println("nie prawidlowa");
			//			odpowiedzi_studenta=odpowiedzi_studenta(numer_studenta,zmienna_do_odpo,odpowiedzi_studenta);
						tab[p]=0;
						p++;
					}	
				}
				if(p == 4)//-----------gdy mam wszystkie odpowiedzi
				{
					
					double wynik=0;
					for(int i =0 ; i<4 ; i++)
					{
						wynik=wynik+tab[i];
					}
					if(wynik>0)
					{
					wynik=((wynik/4)*100);
					out.println("Twoj wynik to "+(int)wynik+"%");
					zapis.println("Student o numerze: "+numer_studenta+" otrzymal wynik "+(int)wynik+"%");
					exe1 .submit(new Baza( "INSERT INTO `odpowiedzi_studenta` (`numer_studenta`, `ocena`, `odpowiedzi`) VALUES ('"+numer_studenta+"', '"+wynik+"', '"+odpowiedzi_studenta+"')" , "dodaj" ));
					}else{
					out.println("Twoj wynik to 0%");
					zapis.println("Student o numerze: "+numer_studenta+" otrzymal wynik 0%");
					exe1.submit(new Baza( "INSERT INTO `odpowiedzi_studenta` (`numer_studenta`, `ocena`, `odpowiedzi`) VALUES ('"+numer_studenta+"', '0', '"+odpowiedzi_studenta+"')" , "dodaj" ));
					}
					p++;
				}
			}
			
			zapis.close();
			mySocket.close();		
			System.out.println("\nSERWER " + i +" zakonczyl prace\n");
			///******************
			//UPDATE baza_pytan set odpowiedzi='"++"' where id=2 nadpisuje rekord
			//
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	private void dodaj_poprawne_odpowiedzi(BufferedReader odpowiedzi) throws IOException {
		ExecutorService exe1 = Executors.newFixedThreadPool(1);
		exe1.submit(new Baza( "DELETE FROM `baza_odpowiedzi` WHERE 1" , "dodaj" ));
		String odpowiedzi_do_pytania="";
		for(int i=0;i<4;i++)
		{	
			exe1.submit( new Baza( "INSERT INTO `baza_odpowiedzi` (`id`, `odpowiedzi`) VALUES ('"+i+"',  '"+odpowiedzi.readLine()+"')" , "dodaj" ));  
		}
		
	}

	private void wyslij_z_bazy(PrintWriter out) throws InterruptedException, ExecutionException 
	{
		ExecutorService exe = Executors.newFixedThreadPool(1);		
		out.println(exe.submit(new Baza( "SELECT * FROM `baza_pytan` WHERE 1" , "zwroc" )).get());	
	}
	
	private String odpowiedzi_studenta(String numer_studenta, String zmienna_do_odpo, String odpowiedzi_studenta) throws InterruptedException, ExecutionException 
	{
		ExecutorService exe1 = Executors.newFixedThreadPool(1);
		Future <String> future =exe1.submit(new Baza( "SELECT * FROM `baza_odpowiedzi` WHERE id='"+numer_studenta+"';" , "zwroc" ));
		odpowiedzi_studenta=future.get()+zmienna_do_odpo;		
		exe1.submit(new Baza( "UPDATE `baza_odpowiedzi` SET `odpowiedzi`=\""+odpowiedzi_studenta+"\" WHERE `id`='"+numer_studenta+"';" , "dodaj" ));
		return odpowiedzi_studenta;
	}
}