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

function sendData() {
    let brugernavn = document.getElementById("brugernavn").value
    let adgangskode = document.getElementById("adgangskode").value
    fetch("/send/" + brugernavn + "?adgangskode=" + adgangskode)
        .then((response) => response.json())
        .then(function (data) {
            console.log(data);
        });
}
