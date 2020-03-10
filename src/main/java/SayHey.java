import io.javalin.Javalin;

import java.io.*;

public class SayHey {
    private Javalin app = null;

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
        app = Javalin.create().start(8080);

        // Sets the root to render the index page
        app.get("/", context -> {
            System.out.println("Rendered index.html");
            context.render("index.html");
        });

        // Answers with the script.js file, when the index page looks for the script at ~/script.js
        File file = new File("./src/main/resources/script.js");
        app.get("/script.js", context -> {
            System.out.println("Sent script.js via filestream");
            context.result(new FileInputStream(file));
        });

        // This happens before every call to the REST backend
        app.before(ctx -> {
            System.out.println("Received: " + ctx.method()+" on " + ctx.url());
        });
    }

    private void webUserPaths() {

        // A test GET to send a message with REST
        String hej = "Hej fra java-delen (javalin)";
        app.get("/hej", context -> {
            context.json(hej);
        });

        // A test GET to change site
        app.get("/site2", context -> {
            System.out.println("Rendered index2.html");
            context.render("index2.html");
        });

    }


}