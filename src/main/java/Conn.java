import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// V: тебе нужен корневой пакет, типа ru.nesterchur.tobiplohobot


public class Conn { //V: символы можно не жалеть - DatabaseConnection, например
    public static Connection conn; //V: путаница. Есть твой класс - Conn, a есть переменная conn, которая НЕ ЭТОГО типа.
                                   //есть смысл твой класс назвать как-то по-другому, а переменную - connection
    public static Statement statmt;
    public static ResultSet resSet;

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    //V: если это делать не в конструкторе, а в методе, и назвать метод connect - будет самодокументируемый код
    public static void Conn() throws ClassNotFoundException, SQLException
    {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:DBFIGHT1.db");

        System.out.println("Connect!");
    }

    // --------Создание таблицы--------
    // имя метода должно начинаться с маленькой буквы 
    public static void CreateDB(Long chatId) throws ClassNotFoundException, SQLException
    {
        statmt = conn.createStatement();
        //в одну таблицу
        //скрипты разворачивания базы лучше вынести в *.sql-файлы, и тут их читать и из них разворачивать базу
        statmt.execute("CREATE TABLE if not exists '" + chatId +"' ('name' text, 'nickName' text, 'hill' INT, 'check_damage' INT, 'check_for_today' INT);");
        
        //для логирования лучше использовать slf4j
        System.out.println("Создана таблица: " + chatId);
    }

    // --------Заполнение таблицы--------
    //твой класс выполняет уже как минимум 2 задачи: разворачивание БД И взаимодействие бизнес-логики с БД (ну т.е. инсерты итд)
    //такое как правило в *Dao-классах. Типа ChatsDao.
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
    // DEL
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
