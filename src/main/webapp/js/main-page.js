$(document).ready(function () {
    $('.producers-item').click(function (event) {
        event.preventDefault();
        $('.producers-item').removeClass('active');
        $(this).addClass("active");
        $('.col-md-3').hide();
        var producer = $(this).attr('id');
        if (producer === 'All'){
            $('.col-md-3').show();
        }
        $('.col-md-3.' + producer).show();
        $('.card-text:visible').matchHeight();
    });
});