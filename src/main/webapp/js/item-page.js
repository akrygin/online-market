$(document).ready(function () {
    $('.menu-item').click(function (event) {
        event.preventDefault();
        $('.menu-item').removeClass('active');
        $(this).addClass("active");

        $('.info-item').hide();
        var menu = $(this).attr('id');
        $('.info-item.' + menu).show();
    });
});