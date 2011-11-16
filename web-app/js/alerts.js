function alertsCallback(data){
  $('#alerts').html('<a href="'+data.link+'&redirect='+document.location.href+'" class="'+data.name+'">'+data.name+'</a>');
}