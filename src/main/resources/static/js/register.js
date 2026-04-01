(function(){
  const registerForm = document.getElementById('registerForm');
  const mfaForm = document.getElementById('mfaForm');
  let emailCache = '';

  const registerLoading = document.getElementById('register-loading');
  const mfaLoading = document.getElementById('mfa-loading');
  let loading = false;

  function setLoading(newLoading) {
    loading = newLoading;
    if (loading) {
      registerLoading.classList.remove('hidden');
      mfaLoading.classList.remove('hidden');
    } else {
      registerLoading.classList.add('hidden');
      mfaLoading.classList.add('hidden');
    }
  }

  registerForm.addEventListener('submit', async evt => {
    try {
      evt.preventDefault();
      if (loading)
        return;
      setLoading(true);
      const data = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        role: document.getElementById("role").value
      };
      const res = await fetch("/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type":"application/json"
        },
        body: JSON.stringify(data)
      });
      if (res.ok) {
        emailCache = data.email;

        registerForm.classList.add('hidden');
        mfaForm.classList.remove('hidden');
      } else {
        if (res.status === 429) {
          const retryAfter = res.headers.get('Retry-After');
          alert(`Muitas requisições! Tente novamente em ${retryAfter || 'alguns'} segundos.`);
        } else {
          alert('Erro ao registrar');
        }
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
      const res = await fetch("/api/auth/register/2fa", {
        method:"POST",
        headers:{
          "Content-Type":"application/json"
        },
        body: JSON.stringify({
          email: emailCache,
          code: code
        })
      });
      if (res.ok)
        window.location.href = '/';
      else
        if (res.status === 429) {
          const retryAfter = res.headers.get('Retry-After');
          alert(`Muitas requisições! Tente novamente em ${retryAfter || 'alguns'} segundos.`);
        } else {
          alert('Código inválido');
        }
    } catch(err) {
      console.error(err);
      alert('Um erro ocorreu');
    } finally {
      setLoading(false);
    }
  })
})()
