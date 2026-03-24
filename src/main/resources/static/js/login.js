(function(){
  const loginBtn = document.querySelector('#login-form > button[type=submit]');
  const loginForm = document.getElementById("login-form");
  const mfaForm = document.getElementById("mfa-form");
  const loginLoading = document.getElementById('login-loading');
  const mfaLoading = document.getElementById('mfa-loading');
  let loading = false;
  let userEmail;

  function setLoading(newLoading) {
    loading = newLoading;
    if (loading) {
      loginLoading.classList.remove('hidden');
      mfaLoading.classList.remove('hidden');
    } else {
      loginLoading.classList.add('hidden');
      mfaLoading.classList.add('hidden');
    }
  }

  async function login(email, password) {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email, password })
    });
    let message = '';
    if (!res.ok) {
      if (res.status === 429) {
        const retryAfter = res.headers.get('Retry-After');
        message = `Muitas requisições! Tente novamente em ${retryAfter || 'alguns'} segundos.`;
      } else {
        message = 'Credenciais inválidas';
      }
    }
    return {
      ok: res.ok,
      message,
    };
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
    let message = '';
    if (!res.ok) {
      if (res.status === 429) {
        const retryAfter = res.headers.get('Retry-After');
        message = `Muitas requisições! Tente novamente em ${retryAfter || 'alguns'} segundos.`;
      } else {
        message = 'Código inválido';
      }
    }
    return {
      ok: res.ok,
      message,
    };
  }

  loginBtn.addEventListener('click', async evt => {
    try {
      evt.preventDefault();
      if (loading)
        return;
      setLoading(true);
      const email = document.getElementById("email").value;
      const password = document.getElementById("password").value;
      const res = await login(email, password);
      if (res.ok) {
        userEmail = email;
        loginForm.classList.add("hidden");
        mfaForm.classList.remove("hidden");
      } else {
        alert(res.message);
      }
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    } finally {
      setLoading(false);
    }
  })

  mfaForm.addEventListener('submit', async evt => {
    try {
      evt.preventDefault();
      if (loading)
        return;
      setLoading(true);
      const code = document.getElementById("code").value;
      const res = await verifyCode(userEmail, code);
      if (res.ok)
        window.location.href = '/dashboard';
      else
        alert(res.message);
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    } finally {
      setLoading(false);
    }
  })
})()
