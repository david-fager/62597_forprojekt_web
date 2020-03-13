import Javalin.Server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RunServer {
    private static DateFormat df = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss] ");

    public static void main(String[] args) {
        System.out.println(df.format(Calendar.getInstance().getTimeInMillis()) + "Starting server");

        Server server = new Server();
        server.setupJavalin();
        server.webUserPaths();

        RunServer.printASCII();

        System.out.println(df.format(Calendar.getInstance().getTimeInMillis()) + "Server started");
    }

    private static void printASCII() {
        System.out.println("   ____   ____    _   _   ____    ____    _____     _    ___        ");
        System.out.println("  / ___| |  _ \\  | | | | |  _ \\  |  _ \\  | ____|   / |  / _ \\   ");
        System.out.println(" | |  _  | |_) | | | | | | |_) | | |_) | |  _|     | | | | | |      ");
        System.out.println(" | |_| | |  _ <  | |_| | |  __/  |  __/  | |___    | | | |_| |      ");
        System.out.println("  \\____| |_| \\_\\  \\___/  |_|     |_|     |_____|   |_|  \\___/  ");
        System.out.println("                                                                    ");
    }

}
