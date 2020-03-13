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
