let sessionID;
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
            if (data === 200) {
                window.location.href='/menu';
            }
        });
}

function sendData() {
    let brugernavn = document.getElementById("brugernavn").value
    let adgangskode = document.getElementById("adgangskode").value
    fetch("/send/" + brugernavn + "?adgangskode=" + adgangskode)
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);
        });
}
