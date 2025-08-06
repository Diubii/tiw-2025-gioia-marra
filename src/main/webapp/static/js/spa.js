document.addEventListener("DOMContentLoaded", () => {
    document.addEventListener("click", async(event) => {
        const link = event.target.closest("a");
        if (!link) return;

        event.preventDefault();
        const url = link.getAttribute("href");

        try {
            const response = await fetch(url, {
                headers: {
                    "X-Fragment-Request": "true"
                }
            });

            if (!response.ok) throw new Error("Error while fetching fragment");

            const html = await response.text();
            document.getElementById("fragment-container").innerHTML = html;

            history.pushState(null, "", url);
        }
        catch (e) {
            console.error("SPA error:", e);
        }
    })
})