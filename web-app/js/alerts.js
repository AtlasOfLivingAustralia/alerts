function alertsCallback(data){
  if(data.alertExists){
      $('#alerts').html('<a href="'+data.link+'" class="'+data.name+'">'+data.name+'</a>');
  } else {
    $('#alerts').html('<a href="'+data.link+'&redirect='+document.location.href+'" class="'+data.name+'">'+data.name+'</a>');
  }
}