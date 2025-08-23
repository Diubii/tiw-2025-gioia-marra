let currentFragment = {
    destroy: null
}

executeScripts();

document.addEventListener("DOMContentLoaded", () => {
    document.addEventListener("click", async(event) => {
        const link = event.target.closest("a") || event.target.closest('input[data-spa]');
        if (!link) return;

        event.preventDefault();

        let response = null;
        let url = null;

        const form = link.closest('form');
        try {
            if(form && form.method.toLowerCase() === 'post'){
                url = form.action;
                if(url == null) return;
                disableElement(link);
                const data = Object.fromEntries(new FormData(form).entries());
                response = await postForm(form.action, data);

                if (!response.ok) throw new Error("Error while posting data " + response.text);
            }
            else {
                url = link.getAttribute("href");
                if (url == null) return;
                disableElement(link);
                response = await fetch(url, {
                    headers: {
                        "X-Fragment-Request": "true"
                    }
                });

                if (!response.ok) throw new Error("Error while fetching fragment: " + response.text);
            }

            enableElement(link);
            replaceFragment(await response.text(), url);
        } catch (e) {
            console.error("SPA error:", e);
        }

        link.disabled = false;
    })
})

function disableElement(el) {
    if (!el) return;

    const tag = el.tagName.toLowerCase();

    if (tag === 'button' || tag === 'input' || tag === 'select' || tag === 'textarea') {
        el.disabled = true; // proprietà nativa
    } else if (tag === 'a') {
        el.dataset.hrefBackup = el.href;   // salva href originale
        el.removeAttribute('href');        // rimuovi href
        el.style.pointerEvents = 'none';   // non cliccabile
        el.style.opacity = '0.5';          // opzionale, per indicare disabilitazione
    } else {
        // per altri elementi generici
        el.style.pointerEvents = 'none';
        el.style.opacity = '0.5';
    }
}

function enableElement(el) {
    if (!el) return;

    const tag = el.tagName.toLowerCase();

    if (tag === 'button' || tag === 'input' || tag === 'select' || tag === 'textarea') {
        el.disabled = false;
    } else if (tag === 'a') {
        if (el.dataset.hrefBackup) el.href = el.dataset.hrefBackup; // ripristina href
        el.removeAttribute("data-href-backup");
        el.style.pointerEvents = 'auto';
        el.style.opacity = '1';
    } else {
        el.style.pointerEvents = 'auto';
        el.style.opacity = '1';
    }
}

function executeScripts() { //Rende eseguibili gli script dei fragment
    //Rimuovo tutti tranne spa.js
    document.body.querySelectorAll("body>script").forEach(script => {
        if(script.nodeName.toLowerCase() === "script" && !script.src.includes("spa.js")) script.remove();
    })

    currentFragment.destroy = null;

    document.getElementById("fragment-container").querySelectorAll("script").forEach(async oldScript => {
        const module = await import(oldScript.src);
        module.init();
        currentFragment.destroy = module.destroy;
        /*const newScript = document.createElement("script");
        if (oldScript.src) {
            newScript.src = oldScript.src;
        } else {
            newScript.textContent = oldScript.textContent;
        }
        document.body.appendChild(newScript);*/
        oldScript.remove();
    });
}

async function postForm(url, data){
    const formBody = new URLSearchParams(data).toString();

    return await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "X-Fragment-Request": "true"
        },
        body: formBody
    });
}

export function destroyCurrentFragment(){
    if(currentFragment.destroy != null) currentFragment.destroy()
}

export function replaceFragment(html, url){
    destroyCurrentFragment();
    document.getElementById("fragment-container").innerHTML = html;
    history.pushState(null, "", url);
    executeScripts();
}