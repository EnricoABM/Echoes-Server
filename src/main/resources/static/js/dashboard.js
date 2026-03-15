(function(){
  const logoutBtn = document.getElementById('logout-btn');

  async function logout() {
    const res = await fetch('/api/auth/logout');
    return res.ok;
  }

  logoutBtn.addEventListener('click', async evt => {
    try {
      evt.preventDefault();
      const res = await logout();
      if (res)
        window.location.href = '/';
      else
        alert('Um erro ocorreu');
    } catch(err){
      console.error(err);
      alert('Um erro ocorreu');
    }
  })
})()
