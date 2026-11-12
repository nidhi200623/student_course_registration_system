// Common functions used by all pages

const API = "http://localhost:8080/api/";

// Call the Java backend. data = object -> sent as POST form data
async function api(path, data) {
    const options = data ? { method: "POST", body: new URLSearchParams(data) } : {};
    const res = await fetch(API + path, options);
    const json = await res.json();
    if (json.error) {
        alert(json.error);
        throw new Error(json.error);
    }
    return json;
}

// Logged-in user is kept in the browser (localStorage)
function getUser(role) {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user || user.role !== role) {
        location.href = "index.html";
        return null;
    }
    const el = document.getElementById("userName");
    if (el) el.textContent = user.name;
    return user;
}

function logout() {
    localStorage.removeItem("user");
    location.href = "index.html";
}

// Build a clean responsive HTML table from rows
function showTable(elementId, rows, buttons) {
    const box = document.getElementById(elementId);
    if (!box) return;
    if (!rows || rows.length === 0) {
        box.innerHTML = "<p class='empty'>No records found.</p>";
        return;
    }
    const cols = Object.keys(rows[0]);
    let html = "<div class='table-responsive'><table><thead><tr>";
    cols.forEach(c => html += "<th>" + c.replaceAll("_", " ") + "</th>");
    if (buttons) html += "<th>Action</th>";
    html += "</tr></thead><tbody>";
    rows.forEach(r => {
        html += "<tr>";
        cols.forEach(c => html += "<td>" + (r[c] ?? "-") + "</td>");
        if (buttons) html += "<td>" + buttons(r) + "</td>";
        html += "</tr>";
    });
    box.innerHTML = html + "</tbody></table></div>";
}
