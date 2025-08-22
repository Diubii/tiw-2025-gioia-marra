import {replaceFragment} from "./spa.js";

export function init() {
    let multiInsertionButton = document.getElementById("multiInsertionButton");
    multiInsertionButton.addEventListener("click", multiInsertionButtonClick);

    let cancelButton = document.getElementById("cancelButton");
    cancelButton.addEventListener("click", cancelButtonClick);

    let confirmButton = document.getElementById("confirmButton");
    confirmButton.addEventListener("click", postGradesBatch);

    document.querySelectorAll('th').forEach(th => th.addEventListener('click', ev => sort(ev)));
}

export function destroy(){
    let multiInsertionButton = document.getElementById("multiInsertionButton");
    multiInsertionButton.removeEventListener("click", cancelButtonClick);

    let cancelButton = document.getElementById("cancelButton");
    cancelButton.removeEventListener("click", cancelButtonClick)

    let confirmButton = document.getElementById("confirmButton");
    confirmButton.removeEventListener("click", postGradesBatch);

    document.querySelectorAll('th').forEach(th => th.removeEventListener('click', ev => sort(ev)));
}

function multiInsertionButtonClick(){
    let multiInsertionModal = document.getElementById("multiInsertionModal");
    multiInsertionModal.hidden = false;
}

function cancelButtonClick(){
    let multiInsertionModal = document.getElementById("multiInsertionModal");
    multiInsertionModal.hidden = true;
}

let sending = false;

function postGradesBatch(){
    if(sending) return;
    sending = true;

    let rows = document.querySelectorAll("#multiInsertionTable tbody tr");
    let urlSearchParams = new URLSearchParams();

    rows.forEach(row => {
        urlSearchParams.append("studentNumbers", row.cells[0].textContent.trim());
        urlSearchParams.append("grades", row.cells[2].querySelector("select").value);
    });

    fetch("grade-student", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "X-Fragment-Request": "true"
        },
        body: urlSearchParams.toString()
    }).then(async r => {
        replaceFragment(await r.text(), window.location.href);
        sending = false;
    })
}

let currentSpanElement = null;
let asc = true;

function sort(event){
    if(currentSpanElement != null) currentSpanElement.remove();

    const th = event.currentTarget;
    const table = th.closest('table');
    const tbody = table.querySelector('tbody');
    Array.from(tbody.querySelectorAll('tr'))
        .sort(comparer(Array.from(th.parentNode.children).indexOf(th), asc = !asc))
        .forEach(tr => tbody.appendChild(tr));

    const span = document.createElement('span');
    const img = document.createElement('img');
    img.src = asc ? "static/svg/down.svg" : "static/svg/up.svg";
    img.style.width = "1em";
    img.style.height = "1em";
    span.appendChild(img);
    th.firstElementChild.appendChild(span);
    currentSpanElement = span;
}

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