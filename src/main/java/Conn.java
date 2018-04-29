import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Conn {
    public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public static void Conn() throws ClassNotFoundException, SQLException
    {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:DBFIGHT1.db");

        System.out.println("Connect!");
    }

    // --------Создание таблицы--------
    public static void CreateDB(Long chatId) throws ClassNotFoundException, SQLException
    {
        statmt = conn.createStatement();
        statmt.execute("CREATE TABLE if not exists '" + chatId +"' ('name' text, 'nickName' text, 'hill' INT, 'check_damage' INT, 'check_for_today' INT);");

        System.out.println("Создана таблица: " + chatId);
    }

    // --------Заполнение таблицы--------
    public static void WriteDBMessage(String user, String messagetext) throws SQLException
    {
        statmt = conn.createStatement();
        statmt.execute("INSERT INTO 'messages' ('user', 'messagetext') VALUES ('" + user + "', '" + messagetext + "'); ");
    }

    public static void addNewUser(Long chatId, String user) throws SQLException {
        statmt = conn.createStatement();
        statmt.execute("INSERT INTO '" + chatId +"' ('name', 'hill', 'check_damage', 'check_for_today') VALUES ('" + user + "', '30', '1', '1'); ");
        System.out.println("Пользователь " + user + " успешно добавлен.");
    }

    public static Boolean checkNewUser(Long chatId, String user) throws SQLException {
        String name = "";
        statmt = conn.createStatement();
        resSet = statmt.executeQuery("SELECT name from '" + chatId + "' where name like '" + user + "' LIMIT 1; " );

        while(resSet.next()) {
            name = resSet.getString("name");
        }
        if (name == "") return true;
        else return false;
    }

    public static void damageUserDb(Long chatId, String user, int damage) throws SQLException {
        statmt = conn.createStatement();
        resSet = statmt.executeQuery("SELECT hill from '" + chatId + "' where name like '" + user + "'; ");
        String hill = "";
        while(resSet.next()) {
            hill = resSet.getString("hill");
        }
        System.out.println(hill);
        int alterHill = Integer.parseInt(hill);
        System.out.println(alterHill);
        alterHill -= damage;
        System.out.println(alterHill);
        statmt.execute("UPDATE '" + chatId + "' SET hill = " + alterHill + " where name like '" + user + "'; ");

    }

    // -------- Вывод таблицы--------CREATE TABLE `messages` ( `user` TEXT, `messagetext` TEXT )
    public static void ReadDB() throws ClassNotFoundException, SQLException
    {
        resSet = statmt.executeQuery("SELECT * FROM users");

        while(resSet.next())
        {
            int id = resSet.getInt("id");
            String  name = resSet.getString("name");
            String  phone = resSet.getString("phone");
            System.out.println( "ID = " + id );
            System.out.println( "name = " + name );
            System.out.println( "phone = " + phone );
            System.out.println();
        }

        System.out.println("Таблица выведена");
    }

    // --------Закрытие--------
    public static void CloseDB() throws ClassNotFoundException, SQLException
    {
        conn.close();
        statmt.close();
        resSet.close();

        System.out.println("Соединения закрыты");
    }

}
