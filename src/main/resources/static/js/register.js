(function(){
  const registerForm = document.getElementById('registerForm');
  const mfaForm = document.getElementById('mfaForm');
  let emailCache = '';

  registerForm.addEventListener('submit', async evt => {
    evt.preventDefault();
    const data = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        role: document.getElementById("role").value
    };
    console.log(data)
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
      alert('Erro ao registrar');
    }
  })

  mfaForm.addEventListener('submit', async evt => {
    evt.preventDefault();
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
      alert('Código inválido');
  })
})()
