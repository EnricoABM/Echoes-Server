(function(){
  const loginBtn = document.querySelector('#login-form > button[type=submit]');
  const loginForm = document.getElementById("login-form");
  const mfaForm = document.getElementById("mfa-form");
  let userEmail;

  async function login(email, password) {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email, password })
    });
    return res.ok;
  }

  async function verifyCode(email, code) {
    const res = await fetch("/api/auth/login/2fa", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        email,
        code,
      })
    });
    return res.ok;
  }

  loginBtn.addEventListener('click', async evt => {
    try {
      evt.preventDefault();
      const email = document.getElementById("email").value;
      const password = document.getElementById("password").value;
      const res = await login(email, password);
      if (res) {
        userEmail = email;
        loginForm.classList.add("hidden");
        mfaForm.classList.remove("hidden");
      } else
        alert('Email ou senha inválidos');
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    }
  })

  mfaForm.addEventListener('submit', async evt => {
    try {
      evt.preventDefault();
      const code = document.getElementById("code").value;
      const res = await verifyCode(userEmail, code);
      if (res)
        window.location.href = '/dashboard';
      else
        alert("Código inválido ou expirado");
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    }
  })
})()
