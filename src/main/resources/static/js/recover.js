(function(){
  const recoverForm = document.getElementById("recover-form");
  const resetForm = document.getElementById("reset-form");

  const recoverBtn = document.querySelector('#recover-form > button');

  let userEmail;

  async function sendCode(email) {
    const res = await fetch("/api/password/forgot", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email })
    });
    return res.ok;
  }

  async function resetPassword(email, code, password) {
    const res = await fetch("/api/password/reset", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        email,
        code,
        newPassword: password,
        confirmPassword: password,
      })
    });
    return res.ok;
  }

  // STEP 1
  recoverBtn.addEventListener('click', async evt => {
    evt.preventDefault();

    try {
      const email = document.getElementById("email").value;
      const res = await sendCode(email);

      if (res) {
        userEmail = email;
        recoverForm.classList.add("hidden");
        resetForm.classList.remove("hidden");
      } else {
        alert("Erro ao enviar código");
      }

    } catch (err) {
      console.error(err);
      alert("Um erro ocorreu");
    }
  });

  // STEP 2
  resetForm.addEventListener('submit', async evt => {
    evt.preventDefault();

    try {
      const code = document.getElementById("code").value;
      const password = document.getElementById("new-password").value;
      const confirm = document.getElementById("confirm-password").value;
      if (password !== confirm)
        return alert("As senhas devem ser equivalentes");

      const res = await resetPassword(userEmail, code, password);

      if (res) {
        alert("Senha redefinida com sucesso");
        window.location.href = "/";
      } else {
        alert("Código inválido ou expirado");
      }

    } catch (err) {
      console.error(err);
      alert("Um erro ocorreu");
    }
  });

})();
