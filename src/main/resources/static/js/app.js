function addRow(containerId, templateId) {
    const container = document.getElementById(containerId);
    const template = document.getElementById(templateId);
    if (!container || !template) {
        return;
    }
    const source = template.querySelector(".repeat-row");
    if (!source) {
        return;
    }
    container.appendChild(source.cloneNode(true));
    reindex(container);
}

function removeRow(button) {
    const row = button.closest(".repeat-row");
    const container = row && row.parentElement;
    if (!row || !container) {
        return;
    }
    row.remove();
    reindex(container);
}

function reindex(container) {
    container.querySelectorAll(":scope > .repeat-row").forEach((row, index) => {
        row.querySelectorAll("[name]").forEach((field) => {
            field.name = field.name.replace(/\[(?:\d+|__INDEX__)\]/, "[" + index + "]");
        });
    });
}

function syncDepartmentParents() {
    const company = document.querySelector("[name=companyId]");
    const parent = document.querySelector("[name=parentDepartmentId]");
    const self = document.querySelector("[name=id]");
    if (!company || !parent) {
        return;
    }
    const companyId = company.value;
    const selfId = self ? self.value : "";
    Array.from(parent.options).forEach((option) => {
        if (!option.value) {
            option.hidden = false;
            return;
        }
        const sameCompany = option.dataset.company === companyId;
        const notSelf = option.value !== selfId;
        option.hidden = !(sameCompany && notSelf);
        if (option.hidden && parent.value === option.value) {
            parent.value = "";
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const company = document.querySelector("form.department-form [name=companyId]");
    if (company) {
        company.addEventListener("change", syncDepartmentParents);
        syncDepartmentParents();
    }
});
