var currentSpanElement = null;

document.querySelectorAll('th').forEach(th => th.addEventListener('click', (() => {
    if(currentSpanElement != null) currentSpanElement.remove();

    const table = th.closest('table');
    const tbody = table.querySelector('tbody');
    Array.from(tbody.querySelectorAll('tr'))
        .sort(comparer(Array.from(th.parentNode.children).indexOf(th), this.asc = !this.asc))
        .forEach(tr => tbody.appendChild(tr));

    const span = document.createElement('span');
    const img = document.createElement('img');
    img.src = this.asc ? "static/svg/down.svg" : "static/svg/up.svg";
    img.style.width = "1em";
    img.style.height = "1em";
    span.appendChild(img);
    th.firstElementChild.appendChild(span);
    currentSpanElement = span;
})));

function getCellValue(tr, idx) {
    return tr.children[idx].innerText || tr.children[idx].textContent;
}

function comparer(idx, asc) {
    return function (a, b) {
        let v1 = getCellValue(asc ? a : b, idx);
        let v2 = getCellValue(asc ? b : a, idx);

        // Confronto numerico
        if (v1 !== '' && v2 !== '' && !isNaN(v1) && !isNaN(v2)) {
            return v1 - v2;
        }
        // Confronti misti, nel caso crescente metto prima le stringhe, nel caso decrescente metto prima i numeri in ordine
        else if(isNaN(v1) && !isNaN(v2)){
            return -1;
        }
        else if(!isNaN(v1) && isNaN(v2)){
            return 1;
        }
        // Confronto stringhe
        else {
            return v1.toString().localeCompare(v2);
        }
    };
}