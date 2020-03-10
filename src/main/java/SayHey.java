import io.javalin.Javalin;
import java_common.rmi.IConnectionHandlerRMI;

import java.rmi.Naming;

public class SayHey {
    private Javalin app = null;
    private IConnectionHandlerRMI javaprogram = null;

    public static void main(String[] args) {
        SayHey sayHey = new SayHey();
        sayHey.setupJavalin();
        sayHey.webUserPaths();
    }

    private void setupJavalin() {
        if (app != null) {
            return;
        }

        // Starts the server
        app = Javalin.create(javalinConfig -> javalinConfig.addStaticFiles("webapp")).start(42069);

        // This happens before every call to the REST backend
        app.before(ctx -> {
            System.out.println("Received: " + ctx.method()+" on " + ctx.url());
        });

        // This is the connection via RMI to the javaprogram
        try {
            javaprogram = (IConnectionHandlerRMI) Naming.lookup("rmi://localhost:9920/hangman_local");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void webUserPaths() {

        // A test GET to send a message with REST
        String hej = "Hej fra java-delen (javalin)";
        app.get("/hej", context -> {
            String temp = javaprogram.getWord(0);
            context.json(hej);
        });

        // A test GET to change site
        app.get("/site2", context -> {
            System.out.println("Rendered index2.html");
            context.render("webapp/index2.html");
        });

    }


}