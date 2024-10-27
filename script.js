document.getElementById("signupForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    alert(`Thank you, ${name}! We will contact you soon at ${email}.`);
});

document.querySelectorAll(".learn-more").forEach(button => {
    button.addEventListener("click", () => {
        alert("More information about this feature is coming soon!");
    });
});
