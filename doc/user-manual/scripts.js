(function () {
    const toggle = document.querySelector('.nav-toggle');
    const nav = document.getElementById('primary-nav');

    if (!toggle || !nav) {
        return;
    }

    toggle.addEventListener('click', () => {
        const expanded = toggle.getAttribute('aria-expanded') === 'true';
        toggle.setAttribute('aria-expanded', String(!expanded));
        nav.classList.toggle('open');
    });

    nav.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', () => {
            if (window.innerWidth <= 720 && nav.classList.contains('open')) {
                nav.classList.remove('open');
                toggle.setAttribute('aria-expanded', 'false');
            }
        });
    });
})();
