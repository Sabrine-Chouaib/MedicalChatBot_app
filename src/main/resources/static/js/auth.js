const API_URL = "http://localhost:8081/api/auth";

function login() {
    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;

    fetch(`${API_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
        .then(res => {
            if (!res.ok) throw new Error("Identifiants invalides");
            return res.json();
        })
        .then(data => {
            localStorage.setItem("token", data.token);
            window.location.href = "index.html";
        })
        .catch(err => {
            document.getElementById("login-error").innerText = err.message;
        });
}

function register() {
    const username = document.getElementById("register-username").value;
    const password = document.getElementById("register-password").value;

    fetch(`${API_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
        .then(res => {
            if (!res.ok) throw new Error("Erreur lors de l'inscription");
            return res.text();
        })
        .then(() => {
            alert("Inscription réussie !");
            window.location.href = "login.html";
        })
        .catch(err => {
            document.getElementById("register-error").innerText = err.message;
        });
}
