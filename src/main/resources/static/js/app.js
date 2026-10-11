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
    const added = container.querySelector(":scope > .repeat-row:last-child [data-row-key]");
    if (added && !added.value) {
        added.value = "new-" + Date.now();
    }
    syncOrgPickers();
}

function removeRow(button) {
    const row = button.closest(".repeat-row");
    const container = row && row.parentElement;
    if (!row || !container) {
        return;
    }
    row.remove();
    reindex(container);
    syncOrgPickers();
}

function reindex(container) {
    container.querySelectorAll(":scope > .repeat-row").forEach((row, index) => {
        row.querySelectorAll("[name]").forEach((field) => {
            field.name = field.name.replace(/\[(?:\d+|__INDEX__|IDX)\]/, "[" + index + "]");
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

function pickerChoices(container) {
    const choices = [{value: "", label: "请选择"}];
    if (!container) {
        return choices;
    }
    container.querySelectorAll(":scope > .repeat-row").forEach((row) => {
        const removed = row.querySelector("input[type=checkbox][name$='.removed']");
        if (removed && removed.checked) {
            return;
        }
        const key = row.querySelector("[data-row-key]");
        if (!key || !key.value) {
            return;
        }
        const bits = [];
        row.querySelectorAll("[data-picker-label]").forEach((field) => {
            if (field.tagName === "SELECT") {
                const selected = field.selectedOptions[0];
                if (selected && selected.value) {
                    bits.push(selected.textContent.trim());
                }
            } else if (field.value) {
                bits.push(field.value.trim());
            }
        });
        choices.push({value: key.value, label: bits.length ? bits.join(" · ") : key.value});
    });
    return choices;
}

function staticChoices(selectId) {
    const source = document.getElementById(selectId);
    if (!source) {
        return [];
    }
    return Array.from(source.options).map((option) => ({value: option.value, label: option.textContent.trim()}));
}

function fillSelect(select, choices) {
    const current = select.value;
    select.replaceChildren();
    choices.forEach((choice) => {
        const option = document.createElement("option");
        option.value = choice.value;
        option.textContent = choice.label;
        select.appendChild(option);
    });
    if (choices.some((choice) => choice.value === current)) {
        select.value = current;
    }
}

function syncOrgPickers() {
    const employments = document.getElementById("employment-rows");
    if (!employments) {
        return;
    }
    const employmentChoices = pickerChoices(employments);
    const assignmentChoices = pickerChoices(document.getElementById("assignment-rows"));
    const managerChoices = assignmentChoices.concat(staticChoices("other-assignments"));
    document.querySelectorAll("select.employment-picker").forEach((select) => fillSelect(select, employmentChoices));
    document.querySelectorAll("select.assignment-picker").forEach((select) => fillSelect(select, assignmentChoices));
    document.querySelectorAll("select.manager-picker").forEach((select) => fillSelect(select, managerChoices));
}

document.addEventListener("DOMContentLoaded", () => {
    const company = document.querySelector("form.department-form [name=companyId]");
    if (company) {
        company.addEventListener("change", syncDepartmentParents);
        syncDepartmentParents();
    }
    const org = document.querySelector("form.employee-org");
    if (org) {
        org.addEventListener("change", syncOrgPickers);
        org.addEventListener("input", syncOrgPickers);
        syncOrgPickers();
    }
});
