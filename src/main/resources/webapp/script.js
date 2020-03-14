let hejNumber = 0;

// For GETting specific website data from java REST
function getHej() {
    fetch('./hej')
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);
            document.getElementById("response").innerHTML = data + " " + ++hejNumber;
        });
}

function login() {
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    fetch("/login/" + username + "?password=" + password)
        .then((response) => response.status)
        .then(function (data) {
            console.log(data);
            if (data === 202) {
                window.location.href='/menu';
            } else if (data === 401) {
                document.getElementById("login-error").innerHTML = "Forkert brugernavn eller adgangskode.";
            }
        });
}

function sendData() {
    let tekst1 = document.getElementById("tekst1").value
    let tekst2 = document.getElementById("tekst2").value
    fetch("/send/" + tekst1 + "?tekst2=" + tekst2)
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);
        });
}

function forgotlogin() {
    let username = document.getElementById("username2").value;
    let message = document.getElementById("message").value;
    fetch("/login/forgot/" + username + "?message=" + message)
        .then((response) => response.status)
        .then(function (data) {
            console.log(data);
        });
}

function accountSettings() {
    window.location.href='/account';
}

function typeGame() {
    window.location.href = '/hangman';
}

function drGame() {
    window.location.href = '/hangman/dr';
}

function standardGame() {
    window.location.href = '/hangman/standard';
}

function changePassword() {
    let oldpassword = document.getElementById("oldpassword").value;
    let newpassword = document.getElementById("newpassword").value;
    fetch('/account/changePassword/' + oldpassword + "?newPassword=" + newpassword)
        .then((response) => response.status)
        .then(function (data) {
            console.log(data);
            if (data === 202) {
                window.location.href = '/./';
            } else if (data === 503) {
                <!-- fejl! -->
            }
        });
}

function menu() {
    window.location.href = '/menu';
}

function getUserInfo() {
    fetch('/account/info')
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);

            document.getElementById("BPBI").innerHTML = data[0].brugernavn;
            document.getElementById("APBI").innerHTML = data[0].adgangskode;
            document.getElementById("BOBI").innerHTML = data[1].brugernavn;
            document.getElementById("AOBI").innerText = data[1].adgangskode;
        });
}

function gameInfo() {
    fetch(window.location.href+'/info')
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);

            document.getElementById("guess").innerText = data[0];
            document.getElementById("used").innerText = data[1];

            if (data[4] === "true"){
                endgame();
            }
        });
}

function showresult() {
    let path = window.location.href;
    path = path.replace("/result", "/info");
    fetch(path)
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);
            if (data[5] === "true"){
                document.getElementById("results").innerText = "vundet";
            } else {
                document.getElementById("results").innerText = "tabt";
            }
        });

}

function endgame() {
    window.location.href = window.location.href + '/result';
}

function guess() {
    let guess = document.getElementById("letter").value;
    fetch(window.location.href + '/guess/' + guess)
        .then((response) => response.status)
        .then(function (data) {
            console.log(data);
            gameInfo();
            // TODO: Handle the status code for whether server had problems or not
        });
    document.getElementById("guess").value = "";
}