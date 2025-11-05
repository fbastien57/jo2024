document.addEventListener('DOMContentLoaded', function() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // 🔁 Fonction utilitaire : recalculer les totaux
    function updateCartTotals() {
        let total = 0;
        document.querySelectorAll('tbody tr').forEach(row => {
            const price = parseFloat(row.querySelector('.price').textContent);
            const quantity = parseInt(row.querySelector('.quantity-input').value);
            const subtotal = price * quantity;
            row.querySelector('.subtotal').textContent = subtotal.toFixed(2);
            total += subtotal;
        });
        document.getElementById('cart-total').textContent = total.toFixed(2);
    }

    // 🔁 Bouton "Mettre à jour"
    document.querySelectorAll('.update-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const row = btn.closest('tr');
            const sessionId = row.dataset.sessionId;
            const quantityInput = row.querySelector('.quantity-input');
            const newQuantity = parseInt(quantityInput.value);

            if (newQuantity <= 0 || isNaN(newQuantity)) {
                alert("Veuillez entrer une quantité valide.");
                return;
            }

            const response = await fetch(`/all/cart/update/${sessionId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({ quantity: newQuantity })
            });

            if (response.ok) {
                const data = await response.json();
                console.log("Response data:", data);
                // data doit contenir : { lineTotal: number, cartTotal: number }
                row.querySelector('.subtotal').textContent = data.lineTotal.toFixed(2);
                document.getElementById('cart-total').textContent = data.cartTotal.toFixed(2);
            } else {
                const error = await response.text();
                alert("Erreur : " + error);
            }
        });
    });

    // 🗑️ Bouton "Supprimer"
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const row = btn.closest('tr');
            const sessionId = row.dataset.sessionId;

            if (!confirm("Voulez-vous vraiment supprimer cet article ?")) return;

            const response = await fetch(`/all/cart/remove/${sessionId}`, {
                method: 'DELETE',
                headers: { [csrfHeader]: csrfToken }
            });

            if (response.ok) {
                row.remove(); // 🔁 supprime la ligne
                updateCartTotals(); // recalcul du total global
            } else {
                const error = await response.text();
                alert("Erreur : " + error);
            }
        });
    });

    // Initialisation au chargement
    updateCartTotals();
});