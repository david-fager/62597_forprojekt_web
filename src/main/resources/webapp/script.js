function toast() {
    fetch('/account/info')
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);

            <!-- makes a little toast for a recognized person -->
            let toastDIV = document.getElementById("toast");
            toastDIV.innerText = "Velkommen " + data[0].fornavn + " " + data[0].efternavn;
            toastDIV.className = "show";
            setTimeout(function (){toastDIV.className = toastDIV.className.replace("show", "")}, 3000);
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

            switch(data[3]) {
                case "1":
                    document.getElementById('image').src = '/./webresources/hangman3.png';
                    break;
                case "2":
                    document.getElementById('image').src = '/./webresources/hangman5.png';
                    break;
                case "3":
                    document.getElementById('image').src = '/./webresources/hangman5_1.png';
                    break;
                case "4":
                    document.getElementById('image').src = '/./webresources/hangman6.png';
                    break;
                case "5":
                    document.getElementById('image').src = '/./webresources/hangman7.png';
                    break;
                case "6":
                    document.getElementById('image').src = '/./webresources/hangman8.png';
                    break;
                case "7":
                    document.getElementById('image').src = '/./webresources/hangman_gameover.png';
                    break;
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
            document.getElementById("word").innerText = data[2];
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
    document.getElementById("letter").value = "";
}

function logout() {
    document.cookie = "sessionID=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    window.location.href = "./";
}