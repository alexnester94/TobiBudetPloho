import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

import static java.lang.Thread.sleep;

public class TelBotFighter extends TelegramLongPollingBot {


    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String query = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String secName = update.getMessage().getFrom().getLastName();
            String chatname = update.getMessage().getChat().getTitle();//наименовая чата
            String finalName = firstName + " " + secName;
            int MAX_DAMAGE = 6;
            int MIN_DAMAGE = 1;

            if (text.substring(0, 4).equals("/hit")) {
                executeHit(chatId, finalName, query, text, MAX_DAMAGE, MIN_DAMAGE);
            } else {
                execDBDefaultAction(chatId, finalName, query);
            }

        }
    }

    private void execDBDefaultAction(Long chatId, String finalName, String query) {
        try {
            Conn.CreateDB(chatId);
            Conn.WriteDBMessage(finalName, query);
            if (Conn.checkNewUser(chatId, finalName))
                Conn.addNewUser(chatId, finalName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

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

    private void execDamageMessage(int MAX_DAMAGE, int MIN_DAMAGE, SendMessage message, Long chatId, String text, int damage) {
        message.
                setChatId(chatId).
                setText(getDamageText(damage, text));
    }

    private void execDamage(int MAX_DAMAGE, int MIN_DAMAGE, SendMessage message, Long chatId, String text) {
        int damage = getRandomDamage(MAX_DAMAGE, MIN_DAMAGE);
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
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void getErrorExecDamage (SendMessage message, Long chatId) {
        message.setChatId(chatId).setText("Ты забыл ввести имя, пример: /hit @dartVaider.");
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
    }

    private void executeHit(Long chatId, String finalName, String query, String text, int MAX_DAMAGE, int MIN_DAMAGE) {
        SendMessage message = new SendMessage();
        execDBDefaultAction(chatId, finalName, query);
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
