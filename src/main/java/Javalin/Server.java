package Javalin;

import io.javalin.Javalin;
import java_common.rmi.IConnectionHandlerRMI;
import org.eclipse.jetty.http.HttpStatus;

import java.rmi.Naming;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Server {
    private Javalin app = null;
    private IConnectionHandlerRMI javaprogram = null;
    private DateFormat df = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss] ");

    private String getTime() {
        return df.format(Calendar.getInstance().getTimeInMillis());
    }

    public void setupJavalin() {
        if (app != null) {
            return;
        }

        // This is the connection via RMI to the javaprogram
        try {
            javaprogram = (IConnectionHandlerRMI) Naming.lookup("rmi://localhost:9920/hangman_local");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Starts the server
        app = Javalin.create(javalinConfig -> javalinConfig.addStaticFiles("webapp")).start(42069);

        // This happens before every call to the REST backend
        app.before(ctx -> {
            int sesID;
            if (ctx.cookieStore("sessionID") != null) {
                sesID = ctx.cookieStore("sessionID");
            } else {
                sesID = 0;
            }
            System.out.println(getTime() + "Received '" + ctx.method() + "' by Session#" + sesID + " on URL:" + ctx.url()
                    + " containing pathparams:" + ctx.pathParamMap() + " queryparams:" + ctx.queryParamMap()
                    + " formparams:" + ctx.formParamMap() + " cookies:" + ctx.cookieMap());
        });
    }

    public void webUserPaths() {

        // PAGE: MAIN (LOGIN) - skips the login screen if the user is recognised by cookie
        app.get("/", context -> {
            if (context.cookieStore("sessionID") != null) {
                System.out.println(getTime() + "User recognized as sessionID: " + context.cookieStore("sessionID") + " redirecting to /menu");
                context.render("webapp/index2.html");
                return;
            }

            context.render("webapp/index.html");
        });

        // BUTTON: LOGIN - checks credentials and gives id via cookie if success
        app.get("/login/:username", context -> {
            int sesID = javaprogram.informConnect();
            String username = context.pathParam("username");
            String password = context.queryParam("password");

            boolean success = javaprogram.login(sesID, username, password);
            if (success) {
                System.out.println(getTime() + "Login success");
                context.cookieStore("sessionID", sesID);
                context.status(HttpStatus.ACCEPTED_202);
            } else {
                System.out.println(getTime() + "Login failed");
                context.status(HttpStatus.UNAUTHORIZED_401);
            }
        });

        // PAGE: FORGOT PASSWORD - loads/renders the page for forgotten password
        app.get("/login/forgot", context -> {
            context.render("webapp/forgot.html");
        });

        // BUTTON: FORGOT PASSWORD - sends the forgot password request to the java program
        app.get("/login/forgot/:username", context -> {
            String username = context.pathParam("username");
            String message = context.queryParam("message");
            if (message == null) {
                message = "";
            }
            javaprogram.forgotPassword(username, message);
        });

        // PAGE: MENU - if the user is authorized to view it, then it renders the menu page otherwise a 401 error
        app.get("/menu", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            context.render("webapp/index2.html");
        });


        //
        app.get("/hangman", context -> {

        });

        //
        app.get("/hangman/:dr", context -> {

        });

        //
        app.get("/hangman/:dr/:guess", context -> {

        });

        //
        app.get("/hangman/:dr/usedLetters", context -> {

        });

        //
        app.get("/hangman/:dr/visibleWord", context -> {

        });

        //
        app.get("/hangman/:dr/actualWord", context -> {

        });

        //
        app.get("/hangman/:dr/result", context -> {

        });


        //
        app.get("/account", context -> {

        });

        //
        app.get("/account", context -> {

        });

    }

    // Examples of how to use the javascript fetch and javalin app
    public void examples() {
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

        // A test to GET with parameters
        app.get("/send/:tekst1", context -> {
            String dab = context.pathParam("tekst1");
            String dab2 = context.queryParam("tekst2");
            if (dab != null) {
                System.out.println(dab);
            }
            if (dab2 != null) {
                System.out.println(dab2);
            }
        });
    }

}