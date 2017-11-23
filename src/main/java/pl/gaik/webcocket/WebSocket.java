package pl.gaik.webcocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.*;

/**
 * Created by monik on 10.11.2017.
 */
@Configuration
@EnableWebSocket
public class WebSocket extends BinaryWebSocketHandler implements WebSocketConfigurer

{
    // tutaj bedziemy zapisywac podpietych uzytkowników
    private Map<String,User> sessions = Collections.synchronizedMap(new HashMap<String,User>()  );
    private  List<String> badWords = Collections.synchronizedList( new ArrayList<String>( Arrays.asList( "test","kurwa" ) ));
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // rejestrujemy taki znacznik naszego połaczenia
        registry.addHandler( this, "/chat").setAllowedOrigins( "*" ) ;// tutaj rejestrujemy endpoinytaki pkt
        // zaczenienia adres url do krórego bedziemy się odwoływać z klienta, dzięki trmu możem dodawac kilka linków wtej klasie mająbyć
        // rzetwarzane wiadomości przez strategi binara, widomości maja byc przesyłane binarbie


    }
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {// obsugije wadomośc
        // przychodzącą binarnie, przyjmuje sesje i wadomośc binarna// metoda obsugujaaca przychodzace wiad.
        User usersending = sessions.get( session.getId() );
        String messageConverted = cenzure( new String( message.getPayload().array()) );
        if(messageConverted.contains( "/" )){
            String[]command= messageConverted.split( " " );
            switch (command[0].substring( 1,command[0].length() )){
                case "addword ":{
                    if (command.length!=2){
                        usersending.getSocketSession().sendMessage( new BinaryMessage( "Złe argumenty".getBytes() ) );
                        break;
                    }
                    badWords.add(command[1]);
                    usersending.getSocketSession().sendMessage( new BinaryMessage( "Dodałeś nowe słowo".getBytes() ) );
                    break;
            }
                case "changeNick":{
                    if (command.length!=2){
                        usersending.getSocketSession().sendMessage( new BinaryMessage( "Złe argumenty".getBytes() ) );
                        break;
                    }
                    usersending.setNick( command[1] );
                }
                case "ban":{
                    if (command.length!=2){
                        usersending.getSocketSession().sendMessage( new BinaryMessage( "Złe argumenty".getBytes() ) );
                        break;
                    }
                    usersending.setNick(command[1]);
                }

            default:{
                usersending.getSocketSession().sendMessage( new BinaryMessage( "Nie ma takiej komendy  ".getBytes() ) );
            }

            }
            return ;
        }
        if (usersending.getNick().isEmpty()) {
            usersending.setNick( messageConverted );
            usersending.getSocketSession().sendMessage( new BinaryMessage( ("Usatwiliśmy twój nick " + messageConverted).getBytes() ) );

        } else {


            for (User user : sessions.values()) { // to pzrelatuje p wszytskich podłaczonych uzytkownikach a nmy do kazdego
                // podeslemy wiadomośc która przyszla
                user.getSocketSession().sendMessage( new BinaryMessage((usersending.getNick()+ messageConverted).getBytes()) );


            }

            // System.out.println("Wiadomość przychodząca"+ new String( message.getPayload().array()));
            // tablica bajtów i zaminiea je na ciąg znaków(String) pobraliśmy zawartoś ci tablice tej wiadomosći o wszystko
            //podaliśmy do argumenta do stringu który pzrekoneweruje tablice bejatów na ciąg znakow
            // dlaczego nie przesyłamy strinków bezposrednio- bo jak bedziemy operowac na wartościach bajtowych
            //jesteśmy w stanie zrobić więcej - zapisać plik, obraz, dzwiek i działa to szybciej
            // dodatkowo z tej kalsy BinaryWebSocketHandler zaimplementowano afterConnectionEstablished gry połacznie zostanie nawiązane
            //
        }

    }
    private void banUser(String nick){
        for (User user:sessions.values()){
            if(user.getNick().equals( nick )){
                try {
                    user.getSocketSession().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private String cenzure(String message){
        String changeMessage = message;
        for (String word:badWords) {


        if(message.contains( word )){
            changeMessage = "Jestem głupi i przeklinam, Przepraszam";
//            changeMessage = message.substring( 0,1 );
//            changeMessage += "***";

        }
    }return changeMessage;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {// gdy ktos podłaczy sie do naczego serwera

        sessions.put(session.getId(),new User( session ));//i kay id sesji, wartość obiekt sassion
        session.sendMessage( new BinaryMessage( "TWOJA PIERWSZA WIAD ZOSTAJE TWOIM NICKIEM ".getBytes()) );
        System.out.println("Zarejesrtowałem nowego uzykownika");

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove( session.getId() );
        System.out.println("Wyrejetrowano uzytkownika");
    }
}
