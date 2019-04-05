document.querySelectorAll("*").forEach(function(el) {
  el.style.webkitUserSelect = 'text';
});
alert('Done, now press and hold any text on the page to select text!');
