function greet() {
    const name = document.getElementById("name").value;
    
    fetch("/hello?name=" + encodeURIComponent(name))
            .then(response => response.text())
            .then(message => {
                document.getElementById("result").innerHTML = message;
    });
}
function getPi() {
    fetch("/pi")
        .then(response => response.text())
        .then(message => {
            document.getElementById("resultPi").innerHTML = "π = " + message;
        });
}

function getE() {
    fetch("/e")
        .then(response => response.text())
        .then(message => {
            document.getElementById("resultE").innerHTML = "e = " + message;
        });
}

function getSquare() {
    const value = document.getElementById("value").value;

    if (value === "" || value === null) {
        document.getElementById("resultSquare").innerHTML = "Introduzca un número";
        return;
    }

    fetch("/square?value=" + encodeURIComponent(value))
        .then(response => response.text())
        .then(message => {
            document.getElementById("resultSquare").innerHTML = message;
        });
}