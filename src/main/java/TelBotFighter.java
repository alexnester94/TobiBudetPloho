import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

import static java.lang.Thread.sleep;

public class TelBotFighter extends TelegramLongPollingBot {


    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            //update.getMessage в локальную переменную
            String text = update.getMessage().getText();
            String query = update.getMessage().getText(); //wtf?
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String secName = update.getMessage().getFrom().getLastName();
            String chatname = update.getMessage().getChat().getTitle();//наименовая чата 
               //коммент лишний - у тебя переменная итак называется chat name
            String finalName = firstName + " " + secName; //fullname?
            int MAX_DAMAGE = 6; //в константы (private final static int - поля)
            int MIN_DAMAGE = 1;

            /*
                saveMessageToDb() (aka execDBDefaultAction)
                if(isHitCommand){
                    executeHit(...)
                }
            */
            if (text.substring(0, 4).equals("/hit")) { // if (isHitCommand(text))
                executeHit(chatId, finalName, query, text, MAX_DAMAGE, MIN_DAMAGE);
            } else {
                execDBDefaultAction(chatId, finalName, query);
            }

        }
    }

    //saveMessage
    private void execDBDefaultAction(Long chatId, String finalName, String query) {
        try {
            Conn.CreateDB(chatId);
            Conn.WriteDBMessage(finalName, query);
            if (Conn.checkNewUser(chatId, finalName))
                Conn.addNewUser(chatId, finalName);
        } catch (SQLException e) {
            e.printStackTrace(); //printStackTrace это плохая практика. Лучше уж в slf4j
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //over refactoring
    private int getTextLength(String text) {
        return text.length();
    }
    private int getRandomDamage(int MAX_DAMAGE, int MIN_DAMAGE) {
        return (int )(Math.random() * MAX_DAMAGE + MIN_DAMAGE);
    }

    private String getKickWord(int damage) {
        int div100 = damage % 100;
        if (div100 >= 10 && div100 <= 20) return "ударов"; 
        else { 
            int div10 = damage % 10;
            if (div10 == 1)
               return "удар";
            } else if(div10 > 1 && div10 <5) {
               return "удара";
            } else return "ударов";
        } 
    }

    private String getDamageText(int damage, String text) {
        return "Сегодня на " + text.substring(5, text.length()) + " напали фанаты хаскеля, нанеся ему: " + damage + " " + getKickWord(damage);
    }

    //side-эффект. У тебя void-метод, который на самом деле меняет message
    //этот метод лучше не выделять - у него нечестный контракт
    private void execDamageMessage(int MAX_DAMAGE, int MIN_DAMAGE, SendMessage message, Long chatId, String text, int damage) {
        message.
                setChatId(chatId).
                setText(getDamageText(damage, text));
    }

    private void execDamage(int MAX_DAMAGE, int MIN_DAMAGE, SendMessage message, Long chatId, String text) {
        int damage = getRandomDamage(MAX_DAMAGE, MIN_DAMAGE);
        /*      message.
                setChatId(chatId).
                setText(getDamageText(damage, text));
                */
        execDamageMessage(MAX_DAMAGE, MIN_DAMAGE, message, chatId, text, damage);
        execDamageDB(chatId, text, damage, message);
    }

    private void execDamageDB(Long chatId, String text, int damage, SendMessage message) {
        try {
            Conn.damageUserDb(chatId, text.substring(5, text.length()), damage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            execute(message); //лучше в отдельный метод, и вызывать его не здесь
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void getErrorExecDamage (SendMessage message, Long chatId) {
        message.setChatId(chatId).setText("Ты забыл ввести имя, пример: /hit @dartVaider.");
        //+ ", придурок."
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        message.setChatId(chatId).setText("Попробуй ещё раз.");
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        //дублирование кода - выделить в метод
    }

    private void executeHit(Long chatId, String finalName, String query, String text, int MAX_DAMAGE, int MIN_DAMAGE) {
        SendMessage message = new SendMessage();
        execDBDefaultAction(chatId, finalName, query);
        //if(text.length > MIN_TEXT_LENGTH)
        if (getTextLength(text) > 5) {
            execDamage(MAX_DAMAGE, MIN_DAMAGE, message, chatId, text);
        } else {
            getErrorExecDamage(message, chatId);
        }
    }

    public String getBotUsername() {
        return "MixKombatBot";
    }
	
    public String getBotToken() {
        return 1;
    }

}
