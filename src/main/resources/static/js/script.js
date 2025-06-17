document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const toggleButtons = document.querySelectorAll('#sidebarToggle');

    // Helper to set cookie
    function setCookie(name, value, days) {
        const expires = new Date(Date.now() + days * 864e5).toUTCString();
        document.cookie = name + '=' + encodeURIComponent(value) + '; expires=' + expires + '; path=/';
    }

    toggleButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
            const isCollapsed = sidebar.classList.contains('collapsed');
            setCookie('sidebar-collapsed', isCollapsed, 7); // valid 7 days
        });
    });
});
