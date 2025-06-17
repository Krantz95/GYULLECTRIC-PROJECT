// 네비게이션 해당되는 페이지 접속 시 해당 메뉴 강조효과 만들기
// 이곳에 스크립트 구현 후
// <th:block layout:fragment="script">
//     <script src="/js/dashboard-charts.js"></script>
// </th:block> 를 이용하여 집어넣기

document.addEventListener('DOMContentLoaded', function () {
    const currentPath = window.location.pathname.split('?')[0];
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (currentPath === href || currentPath.startsWith(href)) {
            link.classList.add('active');
        }
    });
});
