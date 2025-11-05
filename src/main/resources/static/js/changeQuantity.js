function changeQuantity(change) {
    const input = document.getElementById("quantity");
    let current = parseInt(input.value);
    const min = parseInt(input.min);
    const max = parseInt(input.max);

    if (!isNaN(current)) {
        let newValue = current + change;
        if (newValue >= min && newValue <= max) {
            input.value = newValue;
        }
    }
}