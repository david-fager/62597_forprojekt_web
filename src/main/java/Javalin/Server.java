package Javalin;

import brugerautorisation.data.Bruger;
import io.javalin.Javalin;
import java_common.rmi.IConnectionHandlerRMI;
import org.eclipse.jetty.http.HttpStatus;

import java.rmi.Naming;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Server {
    private final boolean DEBUGMODE = true;
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
            if (!DEBUGMODE) {
                boolean recognized = false;
                if (context.cookieStore("sessionID") != null) {
                    recognized = true;
                }

                if (recognized) {
                    System.out.println(getTime() + "User recognized as sessionID: " + context.cookieStore("sessionID") + " redirecting to /menu");
                    context.redirect("/menu");
                } else {
                    context.redirect("/login");
                }
            } else {
                context.redirect("/login");
            }
        });


        // PAGE: LOGIN - the login screen is rendered if the user is not recognised by cookie
        app.get("/login", context -> {
            if (!DEBUGMODE) {
                boolean recognized = false;
                if (context.cookieStore("sessionID") != null) {
                    recognized = true;
                }

                if (recognized) {
                    System.out.println(getTime() + "User recognized as sessionID: " + context.cookieStore("sessionID") + " redirecting to /menu");
                    context.redirect("/menu");
                } else {
                    context.render("webapp/login.html");
                }
            } else {
                context.render("webapp/login.html");
            }
        });

        // BUTTON: LOGIN - checks credentials and gives id via cookie if success
        app.get("/login/:username", context -> {
            boolean recognized = false;
            if (context.cookieStore("sessionID") != null) {
                recognized = true;
            }

            if (recognized) {
                System.out.println(getTime() + "User recognized as sessionID: " + context.cookieStore("sessionID") + " redirecting to /menu");
                context.status(HttpStatus.ACCEPTED_202);
            } else {
                int sesID = javaprogram.informConnect();
                String username = context.pathParam("username");
                String password = context.queryParam("password");

                boolean success = javaprogram.login(sesID, username, password);
                if (success) {
                    System.out.println(getTime() + "Login success");
                    context.cookieStore("sessionID", sesID);
                    context.status(HttpStatus.ACCEPTED_202);
                    context.render("webapp/startside.html");
                } else {
                    System.out.println(getTime() + "Login failed");
                    context.status(HttpStatus.UNAUTHORIZED_401);
                }
            }
        });

        // PAGE: FORGOT PASSWORD - loads/renders the page for forgotten password
        app.get("/login/forgot", context -> {
            context.render("webapp/glemtLogin.html");
        });

        // BUTTON: FORGOT PASSWORD - sends the request for forgotten password
        app.get("/login/forgot/:username", context -> {
            String username = context.pathParam("username");
            String message = context.queryParam("message");
            if (message == null) {
                message = "";
            }

            boolean success = javaprogram.forgotPassword(username, message);
            if (success) {
                context.status(HttpStatus.OK_200);
            } else {
                context.status(HttpStatus.SERVICE_UNAVAILABLE_503);
            }
        });


        // PAGE: MENU - if the user is authorized to view it, then it renders the menu page otherwise a 401 error
        app.get("/menu", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            context.render("webapp/startside.html");
        });


        // PAGE: HANGMAN MODE - a page to show the game modes
        app.get("/hangman", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            context.render("webapp/modeSpil.html");
        });

        // PAGE: HANGMAN GAME
        app.get("/hangman/:mode", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            int sesID = context.cookieStore("sessionID");
            boolean success = false;

            String mode = context.pathParam("mode");
            if (mode.equals("dr")) {
                success = javaprogram.startGame(sesID, 1);
                if (success) {
                    context.render("webapp/spil.html");
                }
            } else if (mode.equals("standard")) {
                success = javaprogram.startGame(sesID, 2);
                if (success) {
                    context.render("webapp/spil.html");
                }
            } else {
                context.status(HttpStatus.BAD_REQUEST_400).result("<h1>400 Bad Request</h1>Check the request vs the servers expectation.").contentType("text/html");
            }

            if (!success) {
                context.status(HttpStatus.SERVICE_UNAVAILABLE_503).result("<h1>503 Service Unavailable</h1>The server experienced and error. Contact system admin").contentType("text/html");
            }
        });

        // CALL: GAMEINFO - collect and receive the info about the game
        app.get("/hangman/:mode/info", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            int sesID = context.cookieStore("sessionID");
            String visibleWord = javaprogram.getVisibleWord(sesID);
            String actualWord = javaprogram.getWord(sesID);

            // Build the string of used letters (a, b, c)
            ArrayList<String> letters = javaprogram.getUsedLetters(sesID);
            String usedletters = "";
            if (letters.size() > 0) {
                for (int i = 0; i < letters.size() - 1; i++) {
                    usedletters += (letters.get(i) + ",");
                }
                usedletters = letters.get(letters.size() - 1);
            }

            String[] info = {visibleWord, usedletters, actualWord};
            context.json(info);
        });

        // BUTTON: GUESS -
        app.get("/hangman/:mode/:guess", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            int sesID = context.cookieStore("sessionID");
            String guess = context.pathParam("guess");
            boolean success = javaprogram.guessLetter(sesID, guess);
            if (success) {
                context.status(HttpStatus.ACCEPTED_202);
            } else {
                context.status(HttpStatus.CONFLICT_409);
            }
        });

        //
        app.get("/hangman/:mode/result", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            // TODO: Mangler
        });


        // PAGE: ACCOUNT - a page to view account info and send a change password request
        app.get("/account", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            context.render("webapp/kontoIndstillinger.html");
        });

        // CALL: ACCOUNT INFO - get the information on the given account
        app.get("/account/info", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            int sesID = context.cookieStore("sessionID");
            Bruger fullAccount = javaprogram.getFullUser(sesID);
            Bruger pubAccount = javaprogram.getPublicUser(sesID);
            Bruger[] users = {fullAccount, pubAccount};
            context.json(users);
        });

        // BUTTON: CHANGE PASSWORD REQUEST - the path to send a form to change password
        app.get("/account/changePassword", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
            }

            int sesID = context.cookieStore("sessionID");
            String oldPassword = context.queryParam("oldPassword");
            String newPassword = context.queryParam("newPassword");

            Bruger result = javaprogram.changePassword(sesID, oldPassword, newPassword);
            context.json(result);
        });

    }
}