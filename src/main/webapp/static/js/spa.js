document.addEventListener("DOMContentLoaded", () => {
    document.addEventListener("click", async(event) => {
        const link = event.target.closest("a");
        if (!link) return;

        event.preventDefault();
        const url = link.getAttribute("href");
        if(url == null) return;

        try {
            const response = await fetch(url, {
                headers: {
                    "X-Fragment-Request": "true"
                }
            });

            if (!response.ok) throw new Error("Error while fetching fragment");

            document.getElementById("fragment-container").innerHTML = await response.text();

            history.pushState(null, "", url);
            executeScripts(document.getElementById("fragment-container"));
        }
        catch (e) {
            console.error("SPA error:", e);
        }
    })
})

function executeScripts(el) { //Rende eseguibili gli script dei fragment
    el.querySelectorAll("script").forEach(oldScript => {
        const newScript = document.createElement("script");
        if (oldScript.src) {
            newScript.src = oldScript.src;
        } else {
            newScript.textContent = oldScript.textContent;
        }
        document.body.appendChild(newScript);
        oldScript.remove();
    });
}