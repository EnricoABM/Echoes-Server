(function(){
  const validateLoading = document.getElementById('validate-loading');
  const changeLoading = document.getElementById('change-loading');
  let loading = false;
  function setLoading(newLoading) {
    loading = newLoading;
    if (loading) {
      validateLoading.classList.remove('hidden');
      changeLoading.classList.remove('hidden');
    } else {
      validateLoading.classList.add('hidden');
      changeLoading.classList.add('hidden');
    }
  }

  const newPasswordSection = document.getElementById('newPasswordSection');

  let resetToken = '';
  async function validatePassword(password) {
    const res = await fetch('/api/password/validate', {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ password }),
    })
    let message = '';
    if (!res.ok) {
      if (res.status === 429) {
        const retryAfter = res.headers.get('Retry-After');
        message = `Muitas requisições! Tente novamente em ${retryAfter || 'alguns'} segundos.`;
      } else {
        message = 'Senha incorreta';
      }
      return {
        ok: false,
        message,
      }
    }
    const data = await res.json();
    resetToken = data.token;
    return {
      ok: true,
      message,
    };
  }
  async function changePassword(password) {
    const res = await fetch('/api/password/change', {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        token: resetToken,
        newPassword: password,
        confirmPassword: password,
      }),
    })
    let message = '';
    if (!res.ok) {
      if (res.status === 429) {
        const retryAfter = res.headers.get('Retry-After');
        message = `Muitas requisições! Tente novamente em ${retryAfter || 'alguns'} segundos.`;
      } else {
        message = 'Um erro ocorreu';
      }
    }
    return {
      ok: res.ok,
      message,
    };
  }

  document.getElementById('validate-btn').addEventListener('click', async () => {
    try {
      if (loading)
        return;
      setLoading(true);
      const current = document.getElementById('currentPassword').value;
      const res = await validatePassword(current);
      if (!res.ok)
        return alert(res.message);

      newPasswordSection.classList.remove("hidden");
      startTokenWatcher(resetToken);
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    } finally {
      setLoading(false);
    }
  })
  newPasswordSection.addEventListener('submit', async evt => {
    try {
      evt.preventDefault();
      if (loading)
        return;
      setLoading(true);
      const newPass = document.getElementById('newPassword').value;
      const confirmPass = document.getElementById('confirmPassword').value;
      if (newPass !== confirmPass)
        return alert('As senhas não são equivalentes');
      const res = await changePassword(newPass);
      if (!res.ok)
        alert(res.message);
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    } finally {
      setLoading(false);
      window.location.reload();
    }
  })

  function startTokenWatcher(token) {
    const decoded = parseJwt(token);
    const now = Math.floor(Date.now() / 1000);

    const timeLeft = (decoded.exp - now) * 1000; // ms

    setTimeout(() => {
      resetToken = null;
      newPasswordSection.classList.add("hidden");

      alert("Sessão expirada. Valide sua senha novamente.");
    }, timeLeft);
  }
  function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload);
  }
})()
