import java_common.rmi.IConnectionHandlerRMI;

import java.rmi.Naming;

public class Javalin {
    private io.javalin.Javalin app = null;
    private IConnectionHandlerRMI javaprogram = null;
    private int sessionID;
    public static void main(String[] args) {
        Javalin javalin = new Javalin();
        javalin.setupJavalin();
        javalin.webUserPaths();
    }

    private void setupJavalin() {
        if (app != null) {
            return;
        }

        // Starts the server
        app = io.javalin.Javalin.create(javalinConfig -> javalinConfig.addStaticFiles("webapp")).start(42069);

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
            String temp = javaprogram.getWord(0);
            context.json(hej + temp);
        });

        // A test GET to change site
        app.get("/site2", context -> {
            System.out.println("Rendered index2.html");
            context.render("webapp/index2.html");
        });

    }

}