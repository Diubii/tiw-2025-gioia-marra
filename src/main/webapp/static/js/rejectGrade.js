export function init(){
    let bin = document.getElementById("bin");
    if(bin){
        bin.addEventListener("dragover", ev => binDragOver(ev));
        bin.addEventListener("drop", ev => binDrop(ev));
    }

    let cancelButton = document.getElementById("cancelButton");
    if(cancelButton) cancelButton.addEventListener("click", ev => cancelButtonClick(ev))
}

export function destroy(){
    let bin = document.getElementById("bin");
    if(bin) {
        bin.removeEventListener("dragover", ev => binDragOver(ev));
        bin.removeEventListener("drop", ev => binDrop(ev));
    }

    let cancelButton = document.getElementById("cancelButton");
    if(cancelButton) cancelButton.removeEventListener("click", ev => cancelButtonClick(ev));
}

function binDragOver(event){
    event.preventDefault()
}

function binDrop(event) {
    let rejectionModal = document.getElementById("rejectionModal")
    event.preventDefault();
    rejectionModal.hidden = false;
}

function cancelButtonClick(event){
    let rejectionModal = document.getElementById("rejectionModal")
    rejectionModal.hidden = true;
}