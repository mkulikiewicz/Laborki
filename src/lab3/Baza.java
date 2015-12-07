package lab3;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
public class Baza implements  Callable <String> {

	String  sql_dodaj=null;
	String sql_wyswietl=null;
	String sql_zwroc=null;
	
	java.sql.Connection connection;
	String sql;
	String nazwabazy;
	Statement s ;
	
	public Baza(String nazwabazy)
	{
		if (ladujSterownik())
			System.out.print(" sterownik OK");
		else
			System.exit(1);
		java.sql.Connection connection = connectToDatabase("127.0.0.1",
				"", "root", "");
		if (connection != null)
			System.out.print(" polaczenie OK\n");
		
		Statement s = createStatement(connection);
	
		executeUpdate(s, "Create database if not exists "+nazwabazy+"");
		this.connection = connectToDatabase("127.0.0.1",
				""+nazwabazy+"", "root", "");
		this.s = createStatement(connection);
	}
	
	/**
		 * konstruktor
		 * 
		 * @param sql
		 *            - komenda sql
		 * @param rodzaj
		 *            - rodzaj polcenie "dodaj" , "wyswietl"
	 * @return 
		 */
	
		public  Baza(String sql,String rodzaj){
		
			
			
			if(rodzaj.equals("dodaj"))
				{
					
					sql_dodaj=rodzaj;
					this.sql=sql;
					this.connection = connectToDatabase("127.0.0.1","KM", "root", "");
					this.s = createStatement(connection);
				}
				else if (rodzaj.equals("wyswietl"))
				{	
					this.sql=sql;
					this.connection = connectToDatabase("127.0.0.1","KM", "root", "");
					this.s = createStatement(connection);
					sql_wyswietl=rodzaj;	
					
				}
				else if (rodzaj.equals("zwroc"))
				{	
					this.sql=sql;
					this.connection = connectToDatabase("127.0.0.1","KM", "root", "");
					this.s = createStatement(connection);
					sql_zwroc=rodzaj;	
					
				}
				
				
				
			
		}
		/**
		 * Metoda ³aduje sterownik jdbc
		 * 
		 * @return true/false
		 */
		static boolean ladujSterownik() {
			// LADOWANIE STEROWNIKA
//			System.out.print("Sprawdzanie sterownika:");
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				return true;
			} catch (Exception e) {
				System.out.println("Blad przy ladowaniu sterownika bazy!");
				return false;
			}
		}

			/**
			 * Metoda s³u¿y do nawi¹zania po³¹czenia z baz¹ danych
			 * 
			 * @param adress
			 *            - adres bazy danych
			 * @param dataBaseName
			 *            - nazwa bazy
			 * @param userName
			 *            - login do bazy
			 * @param password
			 *            - has³o do bazy
			 * @return - po³¹czenie z baz¹
			 */
		public static Connection connectToDatabase(String adress,
				String dataBaseName, String userName, String password) {
		//	System.out.print("\nLaczenie z baza danych:");
			String baza = "jdbc:mysql://" + adress + "/" + dataBaseName;
			// objasnienie opisu bazy:
			// jdbc: - mechanizm laczenia z baza (moze byc inny, np. odbc)
			// mysql: - rodzaj bazy
			// adress - adres serwera z baza (moze byc tez w nazwy)
			// dataBaseName - nazwa bazy (poniewaz na serwerze moze byc kilka roznych
			// baz...)
			java.sql.Connection connection = null;
			try {
				connection = DriverManager.getConnection(baza, userName, password);
			} catch (SQLException e) {
				System.out.println("Blad przy ladowaniu sterownika bazy!");
				System.exit(1);
			}
			return connection;
		}
		/**
		 * Zwrocenie danych uzyskanych z zapytaniem select
		 * 
		 * @param r
		 *            - wynik zapytania
		 * @return zwroc
		 * 			  - zwraca String z danymi z zapytania select
		 */
		public static String returnDataFromQuery(ResultSet r) {
			ResultSetMetaData rsmd;
			String zwroc="";
			try {
				rsmd = r.getMetaData();
				int numcols = rsmd.getColumnCount(); // pobieranie liczby kolum
				/**
				 * r.next() - przejœcie do kolejnego rekordu (wiersza) otrzymanych
				 * wyników
				 */
				// wyswietlanie kolejnych rekordow:
				while (r.next()) {
					for (int i = 1; i <= numcols; i++) {
						Object obj = r.getObject(i);
						if (obj != null){
							if(i!=1 && i !=5)
							zwroc=zwroc+obj.toString()+"\n";//nie dodaje 1 i ostatniej lini
							if(i==5)
							zwroc=zwroc+obj.toString();//ostatnia linie dopisz bez znaku \n
						}
					}			
				}
			} catch (SQLException e) {
				System.out.println("Bl¹d odczytu z bazy! " + e.toString());
				System.exit(3);
			}
			return zwroc;
			
		}
		/**
		 * Wyœwietla dane uzyskane zapytaniem select
		 * 
		 * @param r
		 *            - wynik zapytania
		 * @return 
		 */
		public static void printDataFromQuery(ResultSet r) {
			ResultSetMetaData rsmd;
			try {
				rsmd = r.getMetaData();
				int numcols = rsmd.getColumnCount(); // pobieranie liczby kolumn
				// wyswietlanie nazw kolumn:
				for (int i = 1; i <= numcols; i++) {
					System.out.print("\t" + rsmd.getColumnLabel(i) + "\t|");
				}
				System.out.print("\n____________________________________________________________________________\n");
				/**
				 * r.next() - przejœcie do kolejnego rekordu (wiersza) otrzymanych
				 * wyników
				 */
				// wyswietlanie kolejnych rekordow:
				while (r.next()) {
					for (int i = 1; i <= numcols; i++) {
						Object obj = r.getObject(i);
						if (obj != null)
							System.out.print("\t" + obj.toString() + "\t|");
						
							
						else
							System.out.print("\t");
					}
					System.out.println();
					
				}
			} catch (SQLException e) {
				System.out.println("Bl¹d odczytu z bazy! " + e.toString());
				System.exit(3);
			}
			
		}
	 
		/**
		 * Wykonanie kwerendy i przes³anie wyników do obiektu ResultSet
		 * 
		 * @param s
		 *            - Statement
		 * @param sql
		 *            - zapytanie
		 * @return wynik
		 */
		public static ResultSet executeQuery(Statement s, String sql) {
			try {
				return s.executeQuery(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
	 
		/**
		 * tworzenie obiektu Statement przesy³aj¹cego zapytania do bazy connection
		 * 
		 * @param connection
		 *            - po³¹czenie z baz¹
		 * @return obiekt Statement przesy³aj¹cy zapytania do bazy
		 */
		public static Statement createStatement(Connection connection) {
			try {
				return connection.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			;
			return null;
		}
		private static int executeUpdate(Statement s, String sql) {
			try {
				return s.executeUpdate(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return -1;
		}
	 
		/**
		 * Zamykanie po³¹czenia z baz¹ danych
		 * 
		 * @param connection
		 *            - po³¹czenie z baz¹
		 * @param s
		 *            - obiekt przesy³aj¹cy zapytanie do bazy
		 */
		public static void closeConnection(Connection connection, Statement s) {
			try {
				s.close();
				connection.close();
			} catch (SQLException e) {
				System.exit(4);
			}
		}
	 
		
		
		public void dodaj_do_bazy(String sql)
		{	
			executeUpdate(s, sql);
			
			
		}
		public ResultSet wyswietl_z_bazy(String sql)
		{	
			ResultSet r = executeQuery(s, sql);
			return r;
		}
		
	

		@Override
		public String call() throws Exception {
			String zwrot =null;
			if(sql_zwroc != null)
			{	
				ResultSet r = executeQuery(s, sql);
				zwrot = returnDataFromQuery(r);
				try {
					s.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
			if(sql_dodaj != null)
			{
				this.dodaj_do_bazy(sql);
				try {
					s.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(sql_wyswietl != null)
			{
				this.wyswietl_z_bazy(sql);
				try {
					s.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
			closeConnection(connection, s);
			return zwrot;
		}
		
	}

