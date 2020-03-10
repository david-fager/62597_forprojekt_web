package Javalin;

import io.javalin.Javalin;
import java_common.rmi.IConnectionHandlerRMI;

public class Server {
    private Javalin app = null;
    private IConnectionHandlerRMI javaprogram = null;
    private int sessionID = 0;

    public void setupJavalin() {
        if (app != null) {
            return;
        }

        // Starts the server
        app = Javalin.create(javalinConfig -> javalinConfig.addStaticFiles("webapp")).start(42069);

        // This happens before every call to the REST backend
        app.before(ctx -> {
            System.out.println("Received: " + ctx.method()+" on " + ctx.url());
        });

        /*
        // This is the connection via RMI to the javaprogram
        try {
            javaprogram = (IConnectionHandlerRMI) Naming.lookup("rmi://localhost:9920/hangman_local");
        } catch (Exception e) {
            e.printStackTrace();
        }

         */
    }

    public void webUserPaths() {

        //Login
        String userName = "";
        String password = "";
        app.get("/login", context -> {
            sessionID = javaprogram.informConnect();
            boolean temp = javaprogram.login(0, userName, password);
            context.json(temp);
        });

        // A test GET to send a message with REST
        String hej = "Hej fra java-delen (javalin)";
        app.get("/hej", context -> {
            //String temp = javaprogram.getWord(0);
            context.json(hej);
        });

        // A test GET to change site
        app.get("/send", context -> {
            System.out.println("Rendered index2.html");
            context.render("webapp/index2.html");
        });

        app.get("/send/:brugernavn", context -> {
            String dab = context.pathParam("brugernavn");
            String dab2 = context.queryParam("adgangskode");
            if (dab != null) {
                System.out.println(dab);
            }
            if (dab2 != null) {
                System.out.println(dab2);
            }
        });

    }

}